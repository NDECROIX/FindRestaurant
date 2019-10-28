package com.decroix.nicolas.go4lunch.controller.fragments;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.decroix.nicolas.go4lunch.BuildConfig;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.RestaurantHelper;
import com.decroix.nicolas.go4lunch.base.BaseFragment;
import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapViewFragment extends BaseFragment
        implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {


    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 898;
    private static final int DEFAULT_ZOOM = 18;

    private GoogleMap mMap;
    private PlacesClient placesClient;
    private List<Marker> markers;
    private Location mLastKnownLocation;

    public MapViewFragment() {
        // Required empty public constructor
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Places.initialize(getFragmentContext(), BuildConfig.ApiKey);
        placesClient = Places.createClient(getFragmentContext());
        markers = new ArrayList<>();
    }

    @OnClick(R.id.fragment_map_view_fab)
    void onMyLocationClick() {
        getCurrentLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateLocationSetting();
        updateMapStyle();
    }

    /**
     * Update the style map with the style_json
     */
    private void updateMapStyle() {
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getFragmentContext(), R.raw.style_json));
    }

    /**
     * Updating the parameters of the mMap
     */
    @AfterPermissionGranted(PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
    private void updateLocationSetting() {
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (EasyPermissions.hasPermissions(getFragmentContext(), ACCESS_FINE_LOCATION)) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        } else {
            //mMap.setMyLocationEnabled(false);
            getAccessFineLocationPermission();
        }
    }
    /**
     * Get the location permission.
     */
    private void getAccessFineLocationPermission() {
        if (getActivity() != null && ContextCompat.checkSelfPermission(getFragmentContext(),
                ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Get the last known location of the device
     */
    private void getCurrentLocation() {
        LocationServices.getFusedLocationProviderClient(getFragmentContext()).getLastLocation()
                .addOnCompleteListener(locationTask -> {
                    if (locationTask.isSuccessful() && locationTask.getResult() != null) {
                        mLastKnownLocation = locationTask.getResult();
                        LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(),
                                mLastKnownLocation.getLongitude());
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM);
                        mMap.animateCamera(cameraUpdate);
                    }
                }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_get_location)));
    }

    /**
     * Find all restaurants around the device position and display a marker based on it
     */
    private void getCurrentPlaces() {
        List<Place> places = new ArrayList<>();
        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(PLACES_FIELDS).build();
        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        if (EasyPermissions.hasPermissions(getFragmentContext(), ACCESS_FINE_LOCATION)) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FindCurrentPlaceResponse response = task.getResult();
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        List<Place.Type> types = placeLikelihood.getPlace().getTypes();
                        if (types != null && types.contains(Place.Type.RESTAURANT)) {
                            places.add(placeLikelihood.getPlace());
                        }
                    }
                    if (!places.isEmpty()) {
                        addMarkerColor(places);
                    }
                }
            }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_fetch_place)));
        } else {
            getAccessFineLocationPermission();
        }
    }

    /**
     * Create and add a marker on the map
     * @param places restaurant where add the marker
     */
    private void addMarkerColor(@NonNull List<Place> places) {
        for (Place place : places) {
            if (!markerExist(place) && place.getLatLng() != null)
                RestaurantHelper.getRestaurant(place.getId()).addOnCompleteListener(doc -> {
                    int markerResource = R.drawable.ic_marker_white;
                    if (doc.isSuccessful() && doc.getResult() != null) {
                        Restaurant restaurant = doc.getResult().toObject(Restaurant.class);
                        if (restaurant != null && !restaurant.getUsers().isEmpty())
                            markerResource = R.drawable.ic_marker_orange;
                    }
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(place.getLatLng())
                            .icon(BitmapDescriptorFactory.fromBitmap(
                                    Bitmap.createScaledBitmap(((BitmapDrawable) getResources()
                                            .getDrawable(markerResource)).getBitmap(), 80, 110, false)
                            )));
                    marker.setTag(place);
                    markers.add(marker);
                }).addOnFailureListener(this.onFailureListener(getFragmentContext().getResources().getString(R.string.afl_get_restaurant)));
        }
    }

    /**
     * Check if the restaurant passed as parameter have a marker on the map
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
            // get detail
            return true;
        }
        return false;
    }
}