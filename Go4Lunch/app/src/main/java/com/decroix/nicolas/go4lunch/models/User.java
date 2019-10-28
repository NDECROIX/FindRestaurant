package com.decroix.nicolas.go4lunch.models;

import java.util.List;
import java.util.Objects;

/**
 * User model
 * All functions (getters/setters, empty constructor) are required by firestore.
 */
@SuppressWarnings("unused")
public class User {

    private String uid;
    private String username;
    private String urlPicture;
    private String lunchRestaurantID;
    private String lunchRestaurantName;
    private List<String> favouritePlaceID;

    public User(){}
    public User(String uid, String username, String urlPicture,
                String lunchRestaurantID, String lunchRestaurantName, List<String> favouritePlaceID) {
        this.uid = uid;
        this.username = username;
        this.urlPicture = urlPicture;
        this.lunchRestaurantID = lunchRestaurantID;
        this.lunchRestaurantName = lunchRestaurantName;
        this.favouritePlaceID = favouritePlaceID;
    }

    // GETTERS

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getUrlPicture() {
        return urlPicture;
    }

    public String getLunchRestaurantID() {
        return lunchRestaurantID;
    }

    public String getLunchRestaurantName() {
        return lunchRestaurantName;
    }

    public List<String> getFavouritePlaceID() {
        return favouritePlaceID;
    }

    //  SETTERS

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUrlPicture(String urlPicture) {
        this.urlPicture = urlPicture;
    }

    public void setLunchRestaurantID(String lunchRestaurantID) {
        this.lunchRestaurantID = lunchRestaurantID;
    }

    public void setLunchRestaurantName(String lunchRestaurantName) {
        this.lunchRestaurantName = lunchRestaurantName;
    }

    public void setFavouritePlaceID(List<String> favouritePlaceID) {
        this.favouritePlaceID = favouritePlaceID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return uid.equals(user.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);
    }
}
