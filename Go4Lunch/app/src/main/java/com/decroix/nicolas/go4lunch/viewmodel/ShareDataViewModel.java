package com.decroix.nicolas.go4lunch.viewmodel;

import android.content.Context;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.models.RestaurantItem;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

import static com.decroix.nicolas.go4lunch.api.PlacesClientHelper.PLACES_FIELDS;

public class ShareDataViewModel extends ViewModel {

    private MutableLiveData<Location> myLocation;
    private MutableLiveData<User> myUser;
    private MutableLiveData<List<Place>> myPlaces;
    private List<RestaurantItem> myRestaurantItem;
    private List<Place> placesComparator;

    public LiveData<Location> getMyLocation(Context context, boolean reset) {
        if (myLocation == null) {
            myLocation = new MutableLiveData<>();
            loadMyLocation(context);
        } else if (reset){
            loadMyLocation(context);
        }
        return myLocation;
    }

    public LiveData<User> getMyUser(String uid) {
        if (myUser == null) {
            myUser = new MutableLiveData<>();
            loadMyUser(uid);
        }
        return myUser;
    }

    public LiveData<List<Place>> getMyPlaces(PlacesClient placesClient, boolean reset) {
        if (myPlaces == null) {
            myPlaces = new MutableLiveData<>();
            loadMyPlaces(placesClient);
        } else if (reset){
            loadMyPlaces(placesClient);
        }
        return myPlaces;
    }

    private void loadMyUser(String uid) {
        UserHelper.getUserListener(uid).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null) {
                User user = snapshot.toObject(User.class);
                myUser.setValue(user);
            } else {
                myUser.setValue(null);
            }
        });
    }

    private void loadMyLocation(Context context) {
        LocationServices.getFusedLocationProviderClient(context).getLastLocation().addOnCompleteListener(locationTask -> {
            if (locationTask.isSuccessful()) {
                myLocation.setValue(locationTask.getResult());
            }
        });
    }

    private void loadMyPlaces(PlacesClient placesClient) {
        List<Place> placesID = new ArrayList<>();
        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(PLACES_FIELDS).build();
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FindCurrentPlaceResponse response = task.getResult();
                if (response != null && !response.getPlaceLikelihoods().isEmpty()) {
                    for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                        List<Place.Type> placeType = placeLikelihood.getPlace().getTypes();
                        if (placeType != null && placeType.contains(Place.Type.RESTAURANT)) {
                            placesID.add(placeLikelihood.getPlace());
                        }
                    }
                }
                myPlaces.setValue(placesID);
            }
        });
    }

    public void setMyRestaurantItem(List<RestaurantItem> myRestaurantItem) {
        this.myRestaurantItem = new ArrayList<>();
        placesComparator = new ArrayList<>();
        this.myRestaurantItem.addAll(myRestaurantItem);
        if (myPlaces.getValue() != null)
            placesComparator.addAll(myPlaces.getValue());
    }

    public List<RestaurantItem> getMyRestaurantItem() {
        boolean notNull = myPlaces.getValue() != null && placesComparator != null;
        if (notNull && placesComparator.containsAll(myPlaces.getValue()) && !placesComparator.isEmpty()) {
            return myRestaurantItem;
        }
        return null;
    }
}
