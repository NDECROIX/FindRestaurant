package com.decroix.nicolas.go4lunch.models;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.google.android.libraries.places.api.model.Place;

import java.util.Objects;

public class RestaurantItem {
    private final Place place;
    private final Bitmap bitmap;
    private final int count;

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof RestaurantItem)) {
            return false;
        }
        RestaurantItem restaurantItem = (RestaurantItem) obj;
        return Objects.equals(this.place.getId(), restaurantItem.place.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this);
    }

    public Place getPlace() {
        return place;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getCount() {
        return count;
    }

    public RestaurantItem(Place place, Bitmap bitmap, int count) {
        this.place = place;
        this.bitmap = bitmap;
        this.count = count;
    }
}
