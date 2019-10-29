package com.decroix.nicolas.go4lunch.api;


import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import com.decroix.nicolas.go4lunch.controller.activities.MainActivity;
import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;

import static androidx.test.espresso.Espresso.onIdle;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test RestaurantHelper
 */
@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class RestaurantHelperTest {
    @Rule
    public final ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    /**
     * Required for asynchronous task
     */
    private final CountingIdlingResource callOnApiIdl = new CountingIdlingResource("Call on API IdlingResource");

    /**
     * User to perform test
     */
    private User userTest;

    /**
     * Restaurant to perform test
     */
    private Restaurant restaurantTest;

    /**
     * Referred to the task.isSuccessful() to the call on an api
     */
    private boolean taskIsSuccessful;

    /**
     * Setting up the necessary elements for the tests
     */
    @Before
    public void setup() {
        MainActivity activity = rule.getActivity();
        assertNotNull(activity);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assertNotNull(firebaseUser);
        userTest = createUserTest();
        restaurantTest = createRestaurantTest();
        taskIsSuccessful = false;
        IdlingRegistry.getInstance().register(callOnApiIdl);
    }

    /**
     * Unregister on the IdlingResource
     */
    @After
    public void setDown() {
        IdlingRegistry.getInstance().unregister(callOnApiIdl);
    }

    /**
     * Creates a restaurant test
     *
     * @return restaurant test
     */
    @DataPoint
    public Restaurant createRestaurantTest() {
        Restaurant restaurant = new Restaurant();
        restaurant.setName("restaurantNameTest");
        restaurant.setPlaceID("restaurantIdTest");
        restaurant.setAddress("restaurantAddressTest");
        restaurant.setUsers(new ArrayList<>());
        return restaurant;
    }

    /**
     * Creates a user test
     * @return user test
     */
    @DataPoint
    public User createUserTest() {
        return new User("userTestID", "usernameTest", "urlPictureTest", "restaurantIdTest",
                "restaurantNameTest", new ArrayList<>(Collections.singleton("favouritePlaceIdTest")));
    }

    /**
     * Add a restaurant on the firebase database
     */
    @DataPoint
    private void addRestaurant() {
        callOnApiIdl.increment();
        RestaurantHelper.createRestaurant(restaurantTest).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            callOnApiIdl.decrement();
        });
        onIdle();
    }

    /**
     * Deletes a restaurant from the firebase database
     */
    @DataPoint
    private void deleteRestaurant() {
        callOnApiIdl.increment();
        RestaurantHelper.deleteRestaurant(restaurantTest.getPlaceID()).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            callOnApiIdl.decrement();
        });
        onIdle();
    }

    /**
     * Test addRestaurant() from RestaurantHelper
     * Add restaurant on firebase database
     */
    @Test
    public void testAddRestaurant() {
        addRestaurant();
        assertTrue("Add a restaurant to the database", taskIsSuccessful);
        deleteRestaurant();
    }

    /**
     * Test deleteRestaurant() from RestaurantHelper
     * Delete a restaurant from firebase database
     */
    @Test
    public void testDeleteRestaurant() {
        addRestaurant();
        deleteRestaurant();
        assertTrue("Delete restaurant from the database", taskIsSuccessful);
    }

    /**
     * Test getRestaurantCloudFirestore() from RestaurantHelper
     * Retrieves a restaurant from firebase database
     */
    @Test
    public void testGetRestaurant() {
        addRestaurant();
        callOnApiIdl.increment();
        RestaurantHelper.getRestaurant(restaurantTest.getPlaceID()).addOnCompleteListener(result -> {
            if (result.isSuccessful() && result.getResult() != null) {
                Restaurant restaurant = result.getResult().toObject(Restaurant.class);
                taskIsSuccessful = restaurant != null && restaurant.getPlaceID().equals(restaurantTest.getPlaceID());
            }
            callOnApiIdl.decrement();
        });
        onIdle();
        deleteRestaurant();
        assertTrue("Get restaurant from database ", taskIsSuccessful);
    }

    /**
     * Test addUserInRestaurantRegisterCloudFirestore() from RestaurantHelper
     * Add a user in restaurant register from firebase database
     */
    @Test
    public void testAddUserInRestaurantRegister() {
        addRestaurant();
        callOnApiIdl.increment();
        RestaurantHelper.addUserInList(restaurantTest.getPlaceID(), userTest).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            callOnApiIdl.decrement();
        });
        onIdle();
        deleteRestaurant();
        assertTrue("Add user in restaurant register", taskIsSuccessful);
    }

    /**
     * Test removeUserFromRestaurantRegisterCloudFirestore() from RestaurantHelper
     * Removes a user from the restaurant register
     */
    @Test
    public void testRemoveUserFromRestaurantRegister() {
        addRestaurant();
        callOnApiIdl.increment();
        RestaurantHelper.addUserInList(restaurantTest.getPlaceID(), userTest)
                .addOnCompleteListener(result -> callOnApiIdl.decrement());
        onIdle();
        callOnApiIdl.increment();
        RestaurantHelper.removeUserFromList(restaurantTest.getPlaceID(), userTest).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            callOnApiIdl.decrement();
        });
        onIdle();
        deleteRestaurant();
        assertTrue("Remove the user from the restaurant register", taskIsSuccessful);
    }

}
