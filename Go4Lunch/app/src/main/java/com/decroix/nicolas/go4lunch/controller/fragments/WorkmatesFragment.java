package com.decroix.nicolas.go4lunch.controller.fragments;


import android.location.Location;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.decroix.nicolas.go4lunch.BuildConfig;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.base.BaseFragment;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.view.WorkmatesRecyclerViewAdapter;
import com.google.android.libraries.places.api.Places;
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
public class WorkmatesFragment extends BaseFragment implements WorkmatesRecyclerViewAdapter.OnClickUserListener {

    @BindView(R.id.fragment_workmate_recycler_view)
    RecyclerView recyclerView;

    private WorkmatesRecyclerViewAdapter adapter;
    private List<User> users;

    public WorkmatesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
     * Configures the recycler view
     */
    private void configRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getFragmentContext(), DividerItemDecoration.VERTICAL));
        adapter = new WorkmatesRecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Get all users from firestore
     */
    private void getUsers() {
        UserHelper.getUsers().addOnCompleteListener(task -> {
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
     * Add all users in the recycler view
     * @param users users
     */
    private void loadUsersList(List<User> users){
        adapter.updateUsersList(users);
    }

    @Override
    public void onClickUser(String restaurant) {

    }
}
