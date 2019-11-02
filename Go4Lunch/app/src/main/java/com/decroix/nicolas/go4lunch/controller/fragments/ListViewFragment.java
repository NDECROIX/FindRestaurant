package com.decroix.nicolas.go4lunch.controller.fragments;


import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.decroix.nicolas.go4lunch.BuildConfig;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.RestaurantHelper;
import com.decroix.nicolas.go4lunch.base.ToolbarAutocomplete;
import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.decroix.nicolas.go4lunch.models.RestaurantItem;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.test.TestRecyclerView;
import com.decroix.nicolas.go4lunch.view.adapters.AutocompleteRecyclerViewAdapter;
import com.decroix.nicolas.go4lunch.view.adapters.RestaurantRecyclerViewAdapter;
import com.decroix.nicolas.go4lunch.viewmodel.ShareDataViewModel;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * A simple {@link Fragment} subclass.
 */
public class ListViewFragment extends ToolbarAutocomplete implements AutocompleteRecyclerViewAdapter.onClickAutocompleteResultListener {

    @BindView(R.id.fragment_list_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fragment_list_restaurant_tv_is_empty)
    TextView textViewIsEmpty;

    /**
     * Request code for fine location permission
     */
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 898;

    /**
     * Required to test the recycler view
     */
    private TestRecyclerView callbackTest;

    private PlacesClient placesClient;
    private RestaurantRecyclerViewAdapter adapter;
    private RestaurantRecyclerViewAdapter.OnClickRestaurantItemListener callback;
    private ShareDataViewModel model;
    private boolean addRestaurant;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (RestaurantRecyclerViewAdapter.OnClickRestaurantItemListener) context;
        model = ViewModelProviders.of((FragmentActivity) context).get(ShareDataViewModel.class);
    }

    public ListViewFragment() {
        // Required empty public constructor
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
     * Add restaurant in recycler view
     * @param place restaurant
     * @param picture restaurant image
     * @param workmateCount number of workmate on this restaurant
     */
    private void addRestaurantInRecyclerView(Place place, Bitmap picture, int workmateCount) {
        adapter.addPlace(place, picture, workmateCount);
        if (callbackTest != null && adapter.getItemCount() > 0) {
            callbackTest.recyclerViewHaveData();
            callbackTest = null;
        }
        if (addRestaurant){
            addRestaurant = false;
            model.setMyRestaurantItem(adapter.getRestaurants());
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Places.initialize(getFragmentContext(), BuildConfig.ApiKey);
        placesClient = Places.createClient(getFragmentContext());
    }

    @Override
    public void onStart() {
        super.onStart();
        getCurrentLocation();
    }

    /**
     * Get the last known location
     */
    private void getCurrentLocation() {
        model.getMyLocation(getFragmentContext(), false).observe(this, location -> {
            adapter.setMyLocation(location);
            adapter.clearList();
            getCurrentRestaurants();
        });
    }

    /**
     * Find all restaurants around the position of the device
     */
    @AfterPermissionGranted(PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
    private void getCurrentRestaurants() {
        if (EasyPermissions.hasPermissions(getFragmentContext(), ACCESS_FINE_LOCATION)) {
            List<RestaurantItem> restaurantItems = model.getMyRestaurantItem();
            if (restaurantItems == null){
                model.getMyPlaces(placesClient, false, null).observe(this, this::getRestaurantDetails);
            } else {
                adapter.setRestaurants(restaurantItems);
            }
        } else {
            getLocationPermission();
        }
    }

    /**
     * retrieve the details of each restaurant passed in parameter
     * @param placesId restaurants
     */
    private void getRestaurantDetails(List<Place> placesId) {
        if (placesId == null) {
            return;
        }
        for (Place placeId : placesId) {
            if (placeId != null && placeId.getId() != null) {
                FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId.getId(), PLACE_FIELDS);
                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    if (place.getPhotoMetadatas() != null) {
                        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(place.getPhotoMetadatas().get(0))
                                .setMaxWidth(500)
                                .setMaxHeight(250)
                                .build();

                        placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) ->
                                countWorkmates(place, fetchPhotoResponse.getBitmap()))
                                .addOnFailureListener((exception) -> this.onFailureListener(getString(R.string.afl_fetch_photo)));
                    } else {
                        countWorkmates(place, null);
                    }
                }).addOnFailureListener((exception) -> this.onFailureListener(getString(R.string.afl_fetch_place)));
            }
        }
    }

    /**
     * Count the number of workmate on the restaurant passed in parameter
     * @param place restaurant
     * @param bitmap restaurant picture
     */
    private void countWorkmates(@NonNull Place place, Bitmap bitmap) {
        RestaurantHelper.getRestaurant(place.getId()).addOnCompleteListener(doc -> {
            if (doc.isSuccessful() && doc.getResult() != null) {
                Restaurant restaurant = doc.getResult().toObject(Restaurant.class);
                int workmateCount = 0;
                if (restaurant != null) {
                    List<User> users = restaurant.getUsers();
                    workmateCount = restaurant.getUsers().size();
                    for (User user : users){
                        if (user.getUid().equals(getCurrentUserID())){
                            workmateCount -= 1;
                            break;
                        }
                    }
                }
                addRestaurantInRecyclerView(place, bitmap, workmateCount);
            }
        });
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

    /**
     * Add the restaurant passed in parameter to the recycler view.
     * @param restaurant restaurant
     */
    @Override
    public void onClickAutocompleteResult(Restaurant restaurant) {
        this.showToolbar(true);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(restaurant.getPlaceID(), PLACES_FIELDS);
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            addRestaurant = true;
            getRestaurantDetails(Collections.singletonList(place));
        }).addOnFailureListener((exception) -> this.onFailureListener(getString(R.string.afl_fetch_place)));
    }

    /**
     * Required to test the recycler view
     * @param callbackTest callback on IdlingResource
     */
    @VisibleForTesting
    public void registerOnCallBackTest(TestRecyclerView callbackTest) {
        if (adapter.getItemCount() > 0){
            callbackTest.recyclerViewHaveData();
        } else {
            this.callbackTest = callbackTest;
        }
    }

    @Override
    public void onDestroyView() {
        model.setMyRestaurantItem(adapter.getRestaurants());
        showToolbar(true);
        super.onDestroyView();
    }
}
