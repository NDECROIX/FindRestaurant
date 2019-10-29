package com.decroix.nicolas.go4lunch.utils;

import android.graphics.Color;
import android.location.Location;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.OpeningHours;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Functions static useful
 */
public class UsefulFunctions {

    /**
     * Get distance between two LatLng passed in parameter
     * @param latLngPlace LatLng of the place
     * @param myLocation you position
     * @return Distance in m
     */
    public static String getDistance(LatLng latLngPlace, LatLng myLocation) {
        float[] results = new float[1];
        Location.distanceBetween(myLocation.latitude,
                myLocation.longitude,
                latLngPlace.latitude,
                latLngPlace.longitude, results);
        return String.valueOf((int) results[0]).concat("m");
    }

    /**
     * Get opening hours of the actual day
     * @param openingHours opening hours of place
     * @return String of opening hours
     */
    public static String getOpeningHours(OpeningHours openingHours) {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        String result = openingHours.getWeekdayText().get(day);
        result = result.substring(result.indexOf(":") + 1);
        return result;
    }

    /**
     * Subtract 2 to the rating
     * @param ratingPlace the rating of the place
     * @return the final rating
     */
    public static float parseRating(@Nullable Double ratingPlace){
        if (ratingPlace == null){
            return 0;
        }
        return (float) (ratingPlace - 2);
    }

    /**
     * Convert date to date format E HH:mm
     * @param date date of the message
     * @return date formatted
     */
    public static String convertDateToHour(Date date) {
        if (date == null) {
            return "";
        }
        DateFormat dfTime = new SimpleDateFormat(" E HH:mm", Locale.FRANCE);
        return dfTime.format(date);
    }

    /**
     * Calculate color on the user id between range
     * @param userID user id
     * @return the final color
     */
    public static int getColor(String userID) {
        int color = userID.hashCode() % 255;
        return Color.argb(255, color, 200, color);
    }

}
