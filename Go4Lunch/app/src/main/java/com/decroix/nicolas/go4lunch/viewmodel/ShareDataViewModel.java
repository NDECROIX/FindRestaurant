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

    /**
     * Device location
     */
    private MutableLiveData<Location> myLocation;
    /**
     * Current user
     */
    private MutableLiveData<User> myUser;

    /**
     * Places from current location
     */
    private MutableLiveData<List<Place>> myRestaurants;

    /**
     * Places from myRestaurants with details
     */
    private List<RestaurantItem> myRestaurantItem;

    /**
     * Last myRestaurants list
     */
    private List<Place> restaurantsComparator;

    /**
     * Get the current device location
     * @param context App context
     * @param reset True to recall the device location
     * @return My location
     */
    public LiveData<Location> getMyLocation(Context context, boolean reset) {
        if (myLocation == null) {
            myLocation = new MutableLiveData<>();
            loadMyLocation(context);
        } else if (reset){
            loadMyLocation(context);
        }
        return myLocation;
    }

    /**
     * Get my user from Firebase database
     * @param uid User uid
     * @return My user
     */
    public LiveData<User> getMyUser(String uid) {
        if (myUser == null) {
            myUser = new MutableLiveData<>();
            loadMyUser(uid);
        }
        return myUser;
    }

    /**
     * Retrieves restaurant next my location
     * @param placesClient PlaceClient initialized
     * @param reset True to recall google place api
     * @return List of restaurant
     */
    public LiveData<List<Place>> getMyPlaces(PlacesClient placesClient, boolean reset) {
        if (myRestaurants == null) {
            myRestaurants = new MutableLiveData<>();
            loadMyPlaces(placesClient);
        } else if (reset){
            loadMyPlaces(placesClient);
        }
        return myRestaurants;
    }

    /**
     * Call user data from firebase database
     * @param uid User uid
     */
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

    /**
     * Recall the device location
     * @param context App context
     */
    private void loadMyLocation(Context context) {
        LocationServices.getFusedLocationProviderClient(context).getLastLocation().addOnCompleteListener(locationTask -> {
            if (locationTask.isSuccessful()) {
                myLocation.setValue(locationTask.getResult());
            }
        });
    }

    /**
     * Recall GooglePlace api to update restaurant
     * @param placesClient PlaceClient initialized
     */
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
                myRestaurants.setValue(placesID);
            }
        });
    }

    /**
     * Save RestaurantItem in ShareDataViewModel
     * @param myRestaurantItem List of restaurant item
     */
    public void setMyRestaurantItem(List<RestaurantItem> myRestaurantItem) {
        this.myRestaurantItem = new ArrayList<>();
        restaurantsComparator = new ArrayList<>();
        this.myRestaurantItem.addAll(myRestaurantItem);
        if (myRestaurants.getValue() != null)
            restaurantsComparator.addAll(myRestaurants.getValue());
    }

    /**
     * Return the last restaurant item list if unchanged else null
     * @return last restaurant item
     */
    public List<RestaurantItem> getMyRestaurantItem() {
        boolean notNull = myRestaurants.getValue() != null && restaurantsComparator != null;
        if (notNull && restaurantsComparator.containsAll(myRestaurants.getValue()) && !restaurantsComparator.isEmpty()) {
            return myRestaurantItem;
        }
        return null;
    }
}
