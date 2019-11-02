package com.decroix.nicolas.go4lunch.controller.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.decroix.nicolas.go4lunch.BuildConfig;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.RestaurantHelper;
import com.decroix.nicolas.go4lunch.base.ToolbarAutocomplete;
import com.decroix.nicolas.go4lunch.controller.activities.MainActivity;
import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.view.adapters.AutocompleteRecyclerViewAdapter;
import com.decroix.nicolas.go4lunch.viewmodel.ShareDataViewModel;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapViewFragment extends ToolbarAutocomplete
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, AutocompleteRecyclerViewAdapter.onClickAutocompleteResultListener {

    public interface MapViewFragmentInterface {
        void onClickRestaurantMarker(Place restaurant, @Nullable Bitmap bitmap);
    }

    @BindView(R.id.fragment_map_view_fab)
    FloatingActionButton fab;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 898;
    private static final int DEFAULT_ZOOM = 18;

    private PlacesClient placesClient;
    private GoogleMap mMap;
    private MapViewFragmentInterface callbackRestaurantListener;
    private List<Marker> markers;
    private ShareDataViewModel model;
    private User myUser;

    public MapViewFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Places.initialize(getFragmentContext(), BuildConfig.ApiKey);
        placesClient = Places.createClient(getFragmentContext());
        markers = new ArrayList<>();
        model.getMyUser(getCurrentUserID()).observe(this, user -> myUser = user);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_view, container, false);
        ButterKnife.bind(this, view);
        SupportMapFragment supportMapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map_view));
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callbackRestaurantListener = (MapViewFragmentInterface) context;
        model = ViewModelProviders.of((MainActivity) context).get(ShareDataViewModel.class);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateMapStyle();
        updateLocationSetting();
    }

    @OnClick(R.id.fragment_map_view_fab)
    void onMyLocationClick() {
        model.getMyLocation(getFragmentContext(), true);
    }

    /**
     * Get the last known location of the device
     */
    private void getCurrentLocation() {
        if (EasyPermissions.hasPermissions(getFragmentContext(), ACCESS_FINE_LOCATION)) {
            model.getMyLocation(getFragmentContext(), true).observe(this, location -> {
                LatLng latLng = new LatLng(location.getLatitude(),
                        location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
                mMap.animateCamera(cameraUpdate);
                model.getMyPlaces(placesClient, true, location);
            });
        } else {
            getAccessFineLocationPermission();
        }
    }

    /**
     * Update the style map with the style_json
     */
    private void updateMapStyle() {
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getFragmentContext(), R.raw.style_json));
    }

    /**
     * Get detail of the restaurant whose ID is passed as a parameter
     *
     * @param id restaurant ID
     */
    private void getRestaurantDetail(String id) {

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(id, PLACE_FIELDS);

        placesClient.fetchPlace(request).addOnSuccessListener(response -> {
            Place place = response.getPlace();
            List<Place.Type> types = place.getTypes();
            if (types != null && types.contains(Place.Type.RESTAURANT)) {
                if (place.getPhotoMetadatas() != null) {
                    FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(place.getPhotoMetadatas().get(0))
                            .setMaxWidth(500)
                            .setMaxHeight(250)
                            .build();

                    placesClient.fetchPhoto(photoRequest).addOnSuccessListener(fetchPhotoResponse ->
                            callbackRestaurantListener.onClickRestaurantMarker(place, fetchPhotoResponse.getBitmap()))
                            .addOnFailureListener((exception) -> this.onFailureListener(getString(R.string.afl_fetch_photo)));
                } else {
                    callbackRestaurantListener.onClickRestaurantMarker(place, null);
                }
            }
        }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_fetch_place)));
    }

    /**
     * Updating the parameters of the mMap
     */
    @AfterPermissionGranted(PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
    private void updateLocationSetting() {
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (EasyPermissions.hasPermissions(getFragmentContext(), ACCESS_FINE_LOCATION)) {
            mMap.setMyLocationEnabled(true);
            model.getMyPlaces(placesClient, false, null).observe(this, places -> {
                mMap.clear();
                markers.clear();
                addMarkerColor(places);
            });
            getCurrentLocation();
        } else {
            getAccessFineLocationPermission();
        }
    }

    /**
     * Get the location permission.
     */
    private void getAccessFineLocationPermission() {
        if (getActivity() != null && ContextCompat.checkSelfPermission(getFragmentContext(),
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Create and add a marker on the map
     *
     * @param places restaurant where add the marker
     */
    private void addMarkerColor(@NonNull List<Place> places) {
        for (Place place : places) {
            if (!markerExist(place) && place.getLatLng() != null)
                RestaurantHelper.getRestaurant(place.getId()).addOnCompleteListener(doc -> {
                    int markerResource = R.drawable.ic_marker_white;
                    if (doc.isSuccessful() && doc.getResult() != null) {
                        Restaurant restaurant = doc.getResult().toObject(Restaurant.class);
                        if (myUser != null && restaurant != null && !restaurant.getUsers().isEmpty() &&
                                !restaurant.getPlaceID().equals(myUser.getLunchRestaurantID()))
                            markerResource = R.drawable.ic_marker_orange;
                    }
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(place.getLatLng())
                            .icon(BitmapDescriptorFactory.fromBitmap(
                                    Bitmap.createScaledBitmap(((BitmapDrawable) getFragmentContext().getResources()
                                            .getDrawable(markerResource)).getBitmap(), 80, 110, false)
                            )));
                    marker.setTag(place);
                    markers.add(marker);
                }).addOnFailureListener(this.onFailureListener(getFragmentContext().getResources().getString(R.string.afl_get_restaurant)));
        }
    }

    /**
     * Check if the restaurant passed as parameter have a marker on the map
     *
     * @param place restaurant
     * @return true if exist
     */
    private boolean markerExist(Place place) {
        for (Marker marker : markers) {
            if (marker.getTag() == place) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Place place = (Place) marker.getTag();
        if (place != null) {
            getRestaurantDetail(((Place) marker.getTag()).getId());
            return true;
        }
        return false;
    }

    @Override
    public void onClickAutocompleteResult(Restaurant restaurant) {
        this.showToolbar(true);
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(restaurant.getPlaceID(), PLACES_FIELDS);
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            updateCameraPosition(place);
        }).addOnFailureListener((exception) -> this.onFailureListener(getString(R.string.afl_fetch_place)));
    }

    /**
     * Update the camera position on the restaurant passed in parameter and show a marker.
     *
     * @param place restaurant
     */
    private void updateCameraPosition(@NonNull Place place) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), DEFAULT_ZOOM);
        mMap.animateCamera(cameraUpdate);
        addMarkerColor(Collections.singletonList(place));
    }

    @Override
    public void onDestroyView() {
        showToolbar(true);
        super.onDestroyView();
    }
}