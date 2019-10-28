package com.decroix.nicolas.go4lunch.controller.fragments;


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.decroix.nicolas.go4lunch.BuildConfig;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.base.BaseFragment;
import com.decroix.nicolas.go4lunch.view.RestaurantRecyclerViewAdapter;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListViewFragment extends BaseFragment {

    @BindView(R.id.fragment_list_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_list_restaurant_tv_is_empty)
    TextView textViewIsEmpty;

    /**
     * Request code for fine location permission
     */
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 898;

    private PlacesClient placesClient;

    private RestaurantRecyclerViewAdapter adapter;
    private RestaurantRecyclerViewAdapter.OnClickRestaurantItemListener callback;

    public ListViewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Places.initialize(getFragmentContext(), BuildConfig.ApiKey);
        placesClient = Places.createClient(getFragmentContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);
        ButterKnife.bind(this, view);
        configRecyclerView();
        textViewIsEmpty.setVisibility(View.INVISIBLE);
        return view;
    }

    /**
     * Configures the recycler view
     */
    private void configRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getFragmentContext(), DividerItemDecoration.VERTICAL));
        adapter = new RestaurantRecyclerViewAdapter(callback);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Get the last known location
     */
    private void getCurrentLocation() {
        LocationServices.getFusedLocationProviderClient(getFragmentContext())
                .getLastLocation()
                .addOnSuccessListener(result -> {
                    if (result != null) {
                        adapter.setMyLocation(result);
                        adapter.clearList();
                        getCurrentRestaurants();
                    }
                }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_get_location)));
    }

    /**
     * Find all restaurants around the position of the device
     */
    @AfterPermissionGranted(PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
    private void getCurrentRestaurants() {
        List<String> placesID = new ArrayList<>();

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(PLACES_FIELDS).build();

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (ContextCompat.checkSelfPermission(getFragmentContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FindCurrentPlaceResponse response = task.getResult();
                    if (response != null && !response.getPlaceLikelihoods().isEmpty()) {
                        textViewIsEmpty.setVisibility(View.GONE);
                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                            List<Place.Type> placeType = placeLikelihood.getPlace().getTypes();
                            if (placeType != null && placeType.contains(Place.Type.RESTAURANT)) {
                                placesID.add(placeLikelihood.getPlace().getId());
                            }
                        }
                        if (!placesID.isEmpty()) {
                            // get details
                        }
                    } else {
                        textViewIsEmpty.setVisibility(View.VISIBLE);
                    }
                } else {
                    showMessage(getString(R.string.error_unknown_error));
                }
            });
        } else {
            getLocationPermission();
        }
    }

    /**
     * Get the location permission
     */
    private void getLocationPermission() {
        if (getActivity() != null && ContextCompat.checkSelfPermission(getFragmentContext(),
                ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentRestaurants();
            }
        }
    }


}
