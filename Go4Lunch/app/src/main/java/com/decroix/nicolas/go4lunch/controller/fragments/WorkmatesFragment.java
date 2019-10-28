package com.decroix.nicolas.go4lunch.controller.fragments;


import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.decroix.nicolas.go4lunch.BuildConfig;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.base.BaseFragment;
import com.decroix.nicolas.go4lunch.base.ToolbarAutocomplete;
import com.decroix.nicolas.go4lunch.controller.activities.MainActivity;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.view.WorkmatesRecyclerViewAdapter;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.decroix.nicolas.go4lunch.api.UserHelper.getUsers;

/**
 * A simple {@link Fragment} subclass.
 */
public class WorkmatesFragment extends ToolbarAutocomplete implements WorkmatesRecyclerViewAdapter.OnClickUserListener, TextWatcher {

    @BindView(R.id.fragment_workmate_recycler_view)
    RecyclerView recyclerView;

    private WorkmatesRecyclerViewAdapter adapter;
    private PlacesClient placesClient;
    private List<User> users;

    private Location myLocation;

    public WorkmatesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Places.initialize(getFragmentContext(), BuildConfig.ApiKey);
        placesClient = Places.createClient(getFragmentContext());
        users = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        ButterKnife.bind(this, view);
        configRecyclerView();
        getUsers();
        return view;
    }

    /**
     * Get all users from firestore
     */
    private void getUsers() {
        UserHelper.getUsers().addOnCompleteListener( task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                List<User> users = new ArrayList<>();
                for (DocumentSnapshot document : documents) {
                    User user = document.toObject(User.class);
                    if (user != null && !user.getUid().equals(getCurrentUserID())){
                        users.add(document.toObject(User.class));
                    }
                }
                loadUsersList(users);
                this.users.addAll(adapter.getUsers());
            }
        });
    }

    /**
     * Configures the recycler view
     */
    private void configRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getFragmentContext(), DividerItemDecoration.VERTICAL));
        adapter = new WorkmatesRecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Add all users in the recycler view
     * @param users users
     */
    private void loadUsersList(List<User> users){
        adapter.updateUsersList(users);
    }

    @Override
    public void onClickUser(String restaurant) {
        getPlaceDetails(restaurant);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        myLocation = ((MainActivity) context).mLastKnownLocation;
    }

    /**
     * Get details of the restaurant whose ID is passed in parameter
     * @param id restaurant id
     */
    private void getPlaceDetails(String id) {

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(id, PLACE_FIELDS);

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            if (place.getTypes() != null && place.getTypes().contains(Place.Type.RESTAURANT)) {
                if (place.getPhotoMetadatas() != null) {
                    FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(place.getPhotoMetadatas().get(0))
                            .setMaxWidth(500)
                            .setMaxHeight(250)
                            .build();

                    placesClient
                            .fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse ->{
                                // Start detail activity
                            })
                            .addOnFailureListener(this.onFailureListener(getString(R.string.afl_fetch_photo)));
                } else {
                    // Start detail activity
                }
            }
        }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_fetch_place)));
    }

    //-------------------------------------------
    // Text Watcher for filter the recycler view
    //-------------------------------------------

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        String text = charSequence.toString().toLowerCase();
        adapter.clearUsers();
        if (!text.isEmpty()) {
            for (User user : users) {
                if (user.getUsername().toLowerCase().contains(text)) {
                    adapter.addUser(user);
                }
            }
        } else {
            adapter.updateUsersList(users);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }
}
