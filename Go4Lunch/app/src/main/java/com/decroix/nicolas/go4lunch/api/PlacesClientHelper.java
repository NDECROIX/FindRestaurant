package com.decroix.nicolas.go4lunch.api;

import com.decroix.nicolas.go4lunch.controller.activities.DetailActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * class that allows the management of calls on the api google place
 */
public class PlacesClientHelper {

    /**
     * Array of all data field return by the find current place request
     */
    public static final List<Place.Field> PLACES_FIELDS = Arrays.asList(Place.Field.ID, Place.Field.TYPES, Place.Field.LAT_LNG);

    /**
     * Array of all data types returned by a fetch place request
     */
    private static final List<Place.Field> PLACE_FIELDS = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
            Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG,
            Place.Field.TYPES);

    /**
     * Retrieves the restaurant website
     *
     * @param placeID restaurant id
     * @return restaurant website url
     */
    public static Task<FetchPlaceResponse> getWebsiteFromPlace(PlacesClient placesClient, String placeID) {
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, Collections.singletonList(Place.Field.WEBSITE_URI));
        return placesClient.fetchPlace(request);
    }

    /**
     * Retrieves the restaurant phone number
     *
     * @param placeID restaurant id
     * @return restaurant phone number
     */
    public static Task<FetchPlaceResponse> getPhoneFromPlace(PlacesClient placesClient, String placeID) {
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, Collections.singletonList(Place.Field.PHONE_NUMBER));
        return placesClient.fetchPlace(request);
    }

    /**
     * Get detail of the restaurant whose ID is passed as a parameter
     * and start DetailActivity
     *
     * @param restaurantID restaurant ID
     * @param callback     callback to start the detail activity
     */
    public static void startDetailActivity(PlacesClient placesClient, String restaurantID, DetailActivity.StartDetailActivity callback) {
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(restaurantID, PLACE_FIELDS);
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
                            callback.startDetailActivity(place, fetchPhotoResponse.getBitmap()));
                } else {
                    callback.startDetailActivity(place, null);
                }
            }
        });
    }
}
