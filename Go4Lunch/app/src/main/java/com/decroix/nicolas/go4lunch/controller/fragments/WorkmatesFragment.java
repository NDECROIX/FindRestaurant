package com.decroix.nicolas.go4lunch.controller.fragments;


import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.base.BaseFragment;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.view.WorkmatesRecyclerViewAdapter;
import com.google.android.libraries.places.api.net.PlacesClient;

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

    public WorkmatesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_workmates, container, false);
        ButterKnife.bind(this, view);
        //toolbarViewHolder = new ToolbarViewHolder(getActivity());
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

    @Override
    public void onClickUser(String restaurant) {

    }
}
