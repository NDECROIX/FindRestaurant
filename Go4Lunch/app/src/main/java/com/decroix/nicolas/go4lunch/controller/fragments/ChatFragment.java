package com.decroix.nicolas.go4lunch.controller.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.MessageHelper;
import com.decroix.nicolas.go4lunch.api.StorageHelper;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.base.BaseFragment;
import com.decroix.nicolas.go4lunch.models.Message;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.test.TestRecyclerView;
import com.decroix.nicolas.go4lunch.view.adapters.ChatRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends BaseFragment {

    private static final int RC_IMAGE_PERMS = 111;
    private static final int RC_CHOOSE_PHOTO = 222;

    @BindView(R.id.fragment_chat_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_chat_img_btn)
    ImageButton addPictureBtn;
    @BindView(R.id.fragment_chat_message_edit)
    EditText editTextMessage;
    @BindView(R.id.fragment_chat_send_btn)
    ImageButton sendMessageBtn;
    @BindView(R.id.fragment_chat_iv_photo_preview)
    ImageView previewPhoto;
    @BindView(R.id.fragment_chat_tv_chat_is_empty)
    TextView chatIsEmpty;

    private ChatRecyclerViewAdapter adapter;
    private User currentUser;
    private FirebaseAuth mAuth;
    private InputMethodManager inputMethodManager;
    private Uri uriImageSelected;

    /**
     * Required to test the recycler view
     */
    private TestRecyclerView callbackTest;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        getCurrentUserFirestore();
        inputMethodManager = (InputMethodManager) getFragmentContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);
        chatIsEmpty.setVisibility(View.GONE);
        configRecyclerView();
        listenerOnData();
        if (getActivity() != null){
            getActivity().findViewById(R.id.menu_activity_main_search).setVisibility(View.INVISIBLE);
        }
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() != null){
            getActivity().findViewById(R.id.menu_activity_main_search).setVisibility(View.VISIBLE);
        }
    }

    /**
     * Configures the recycler view
     */
    private void configRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatRecyclerViewAdapter(getCurrentUserID());
        recyclerView.setAdapter(adapter);
    }

    /**
     * Get the user data from firestore
     */
    private void getCurrentUserFirestore() {
        if (mAuth.getCurrentUser() == null) {
            return;
        }
        UserHelper.getUser(mAuth.getCurrentUser().getUid()).addOnSuccessListener(task -> {
            if (task != null) {
                currentUser = task.toObject(User.class);
            }
        }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_get_user)));
    }

    /**
     * Create listener on data in Firestore for update chat in realtime
     */
    private void listenerOnData() {
        MessageHelper.getAllMessageForChat().addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && !snapshot.getDocuments().isEmpty()) {
                List<Message> messages = snapshot.toObjects(Message.class);
                updateRecyclerView(messages);
            } else {
                chatIsEmpty.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Update the list of messages
     * @param messages Messages
     */
    private void updateRecyclerView(List<Message> messages){
        adapter.updateMessages(messages);
        recyclerView.smoothScrollToPosition(recyclerView.getBottom());if (this.callbackTest != null){
            this.callbackTest.recyclerViewHaveData();
            this.callbackTest = null;
        }
    }

    @OnClick(R.id.fragment_chat_send_btn)
    void onClickSendMessage() {
        sendMessage();
    }

    /**
     * Send the message typed on firebase
     */
    private void sendMessage() {
        if ((!editTextMessage.getText().toString().isEmpty() || uriImageSelected != null) && currentUser != null) {

            if (this.uriImageSelected == null) {
                MessageHelper.createMessage(editTextMessage.getText().toString(), currentUser)
                        .addOnFailureListener(this.onFailureListener(getString(R.string.afl_create_message)));
            } else {
                sendMessageWithPhoto(editTextMessage.getText().toString());
                this.uriImageSelected = null;
                this.previewPhoto.setImageDrawable(null);
                this.previewPhoto.setVisibility(View.GONE);
            }
            this.editTextMessage.setText("");
            if (this.getView() != null) {
                this.inputMethodManager.hideSoftInputFromWindow(this.getView().getRootView().getWindowToken(), 0);
            }
            this.editTextMessage.clearFocus();
        }
    }

    /**
     * Upload the image to firebase to get the uri and send the message with this uri
     *
     * @param message text to send
     */
    private void sendMessageWithPhoto(final String message) {
        String uuid = UUID.randomUUID().toString();
        StorageHelper.putFileOnFirebaseStorage(uuid, uriImageSelected).addOnSuccessListener(taskSnapshot -> {
            if (taskSnapshot.getMetadata() != null) {
                StorageHelper.getUrlPicture(uuid).addOnCompleteListener(task -> {
                    Uri downloadUri = task.getResult();
                    if (downloadUri != null)
                        MessageHelper.createMessageWithImageForChat(downloadUri.toString(), message, currentUser)
                                .addOnFailureListener(this.onFailureListener(getString(R.string.afl_create_message)));
                });
            }
        }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_put_fil_on_firebase_storage)));
    }

    @OnClick(R.id.fragment_chat_img_btn)
    @AfterPermissionGranted(RC_IMAGE_PERMS)
    void onClickAddPicture() {
        chooseImageFromDevice();
    }

    /**
     * Get the image from the device
     */
    private void chooseImageFromDevice() {
        if (EasyPermissions.hasPermissions(getFragmentContext(), READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, RC_CHOOSE_PHOTO);
        } else {
            getReadExternalStoragePermission();
        }
    }

    private void handleResponse(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                this.uriImageSelected = data.getData();
                this.previewPhoto.setVisibility(View.VISIBLE);
                Glide.with(getFragmentContext())
                        .load(this.uriImageSelected)
                        .apply(RequestOptions.centerCropTransform())
                        .into(this.previewPhoto);
            } else {
                showMessage(getString(R.string.uac_no_image_chosen));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        handleResponse(requestCode, resultCode, data);
    }

    /**
     * Get the read external storage permission.
     */
    private void getReadExternalStoragePermission() {
        if (getActivity() != null && ContextCompat.checkSelfPermission(getFragmentContext(),
                READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{READ_EXTERNAL_STORAGE},
                    RC_IMAGE_PERMS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Required to test the recycler view
     * @param callback callback on IdlingResource
     */
    @VisibleForTesting
    void registerOnCallBackTest(TestRecyclerView callback){
        this.callbackTest = callback;
        if (adapter.getItemCount() > 0){
            this.callbackTest.recyclerViewHaveData();
        } else {
            this.callbackTest = callback;
        }
    }
}
