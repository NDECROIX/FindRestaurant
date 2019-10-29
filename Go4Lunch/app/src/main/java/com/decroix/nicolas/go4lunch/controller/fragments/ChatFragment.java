package com.decroix.nicolas.go4lunch.controller.fragments;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.base.BaseFragment;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.view.ChatRecyclerViewAdapter;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends BaseFragment {

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

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);
        chatIsEmpty.setVisibility(View.GONE);
        configRecyclerView();
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
}
