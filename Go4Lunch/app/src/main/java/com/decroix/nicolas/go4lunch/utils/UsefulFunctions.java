package com.decroix.nicolas.go4lunch.utils;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.text.format.DateUtils;

import androidx.annotation.Nullable;

import com.decroix.nicolas.go4lunch.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.DayOfWeek;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Period;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Functions static useful
 */
public class UsefulFunctions {

    /**
     * Get distance between two LatLng passed in parameter
     *
     * @param latLngPlace LatLng of the place
     * @param myLocation  you position
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
     *
     * @param context App context
     * @param openingHours opening hours of place
     * @return String of opening hours
     */
    public static String getOpeningHours(@Nullable Context context, OpeningHours openingHours) {
        int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        Period period = null;
        if (openingHours.getPeriods().size() > 0){
            day = (openingHours.getPeriods().size() > day) ? day : 0;
            period = openingHours.getPeriods().get(day);
        }
        if (context != null && period != null && period.getOpen() != null && period.getClose() != null) {
            DateFormat dfTime = new SimpleDateFormat(context.getString(R.string.date_format), Locale.FRANCE);
            // if the day is a Sunday and the time is 12 then the restaurant is open 24/7
            if (period.getOpen().getDay().compareTo(DayOfWeek.SUNDAY) == 0 && period.getOpen().getTime().getHours() == 0) {
                return context.getString(R.string.hours_open_h_24);
            }
            // Get hours and minutes
            int openHours = period.getOpen().getTime().getHours();
            int openMinutes = period.getOpen().getTime().getMinutes();
            int closeHours = period.getClose().getTime().getHours();
            int closeMinutes = period.getClose().getTime().getMinutes();

            // Create calendar with open time
            GregorianCalendar openTime = new GregorianCalendar();
            openTime.set(Calendar.HOUR_OF_DAY, openHours);
            openTime.set(Calendar.MINUTE, openMinutes);

            // Create calendar with close time
            GregorianCalendar closeTime = new GregorianCalendar();
            if (openHours > closeHours) closeTime.add(Calendar.DAY_OF_YEAR, 1);
            closeTime.set(Calendar.HOUR_OF_DAY, closeHours);
            closeTime.set(Calendar.MINUTE, closeMinutes);

            // Current time
            Calendar actualTime = Calendar.getInstance();

            // Display according to...
            if (actualTime.getTimeInMillis() <= openTime.getTimeInMillis() && actualTime.getTimeInMillis() < closeTime.getTimeInMillis()) {
                return context.getString(R.string.hours_open_at) + dfTime.format(openTime.getTime());
            } else if (actualTime.getTimeInMillis() <= closeTime.getTimeInMillis() && actualTime.getTimeInMillis() >= openTime.getTimeInMillis()) {
                actualTime.setTimeInMillis(actualTime.getTimeInMillis() + (30 * DateUtils.MINUTE_IN_MILLIS));
                if (actualTime.getTimeInMillis() < closeTime.getTimeInMillis()) {
                    return context.getString(R.string.hours_open_until) + dfTime.format(closeTime.getTime());
                } else {
                    return context.getString(R.string.hours_closing_soon);
                }
            } else {
                return context.getString(R.string.hours_closing);
            }
        }
        // If no period has been registered, display the default time.
        String result = openingHours.getWeekdayText().get(day);
        result = result.substring(result.indexOf(":") + 1);
        return result;
    }

    /**
     * Subtract 2 to the rating
     *
     * @param ratingPlace the rating of the place
     * @return the final rating
     */
    public static float parseRating(@Nullable Double ratingPlace) {
        if (ratingPlace == null) {
            return 0;
        }
        return (float) (ratingPlace - 2);
    }

    /**
     * Convert date to date format E HH:mm
     *
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
     *
     * @param userID user id
     * @return the final color
     */
    public static int getColor(String userID) {
        int color = userID.hashCode() % 255;
        return Color.argb(255, color, 200, color);
    }
}
