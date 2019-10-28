package com.decroix.nicolas.go4lunch.models;

import java.util.List;

/**
 * Restaurant model
 * All functions (getters/setters, empty constructor) are required by firestore.
 */
@SuppressWarnings("unused")
public class Restaurant {

    private String placeID;
    private String name;
    private String address;
    private List<User> users;

    public Restaurant() {
    }

    // GETTERS

    public String getPlaceID() {
        return placeID;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public List<User> getUsers() {
        return users;
    }

    // SETTERS

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

}
