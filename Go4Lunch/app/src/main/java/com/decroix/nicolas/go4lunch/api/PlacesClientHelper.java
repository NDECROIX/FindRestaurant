package com.decroix.nicolas.go4lunch.api;

import android.content.Context;

import com.decroix.nicolas.go4lunch.BuildConfig;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPhotoResponse;
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
     * Array of all data types returned by a fetch place request
     */
    private static final List<Place.Field> PLACE_FIELDS = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
            Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG,
            Place.Field.TYPES);

    private final PlacesClient placesClient;

    /**
     * Initialize the API with the API key
     * @param context The application context
     */
    public PlacesClientHelper(Context context) {
        Places.initialize(context, BuildConfig.ApiKey);
        placesClient = Places.createClient(context);
    }

    /**
     * Retrieves the details of the place whose identifier is passed as a parameter
     * @param id ID of the place
     * @return Task with response
     */
    public Task<FetchPlaceResponse> getPlaceDetails(String id) {
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(id, PLACE_FIELDS);
        return placesClient.fetchPlace(request);
    }

    /**
     * Retrieves the bitmap image of the photo metadata
     * @param bitmap PhotoMetadata of a place
     * @return Task with response
     */
    public Task<FetchPhotoResponse> getBitmapFromPlace(PhotoMetadata bitmap){
        FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(bitmap)
                .setMaxWidth(500)
                .setMaxHeight(250)
                .build();
        return placesClient.fetchPhoto(photoRequest);
    }

    /**
     * Retrieves the restaurant website
     * @param placeID restaurant id
     * @return restaurant website url
     */
    public Task<FetchPlaceResponse> getWebsiteFromPlace(String placeID){
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, Collections.singletonList(Place.Field.WEBSITE_URI));
        return placesClient.fetchPlace(request);
    }

    /**
     * Retrieves the restaurant phone number
     * @param placeID restaurant id
     * @return restaurant phone number
     */
    public Task<FetchPlaceResponse> getPhoneFromPlace(String placeID){
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeID, Collections.singletonList(Place.Field.PHONE_NUMBER));
        return placesClient.fetchPlace(request);
    }
}
