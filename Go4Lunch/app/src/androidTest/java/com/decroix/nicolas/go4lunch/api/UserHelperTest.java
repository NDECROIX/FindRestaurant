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
import java.util.List;

import static androidx.test.espresso.Espresso.onIdle;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test UserHelper
 */
@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class UserHelperTest {
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
     * Creates a user test
     * @return user test
     */
    @DataPoint
    public User createUserTest() {
        return new User("userTestID", "usernameTest", "urlPictureTest", "lunchRestaurantIdTest",
                "lunchRestaurantNameTest", new ArrayList<>(Collections.singleton("favouritePlaceIdTest")));
    }

    /**
     * Add a user to the firebase database
     */
    @DataPoint
    private void addUser() {
        callOnApiIdl.increment();
        UserHelper.createUser(userTest).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            callOnApiIdl.decrement();
        });
        onIdle();
    }

    /**
     * Delete a user from firebase database
     */
    @DataPoint
    private void deleteUser() {
        callOnApiIdl.increment();
        UserHelper.deleteUser(userTest.getUid()).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            callOnApiIdl.decrement();
        });
        onIdle();
    }

    /**
     * Test addUser() from UserHelper
     * Add a user on firebase database
     */
    @Test
    public void testAddUser() {
        addUser();
        assertTrue("Add user to the cloud firestore", taskIsSuccessful);
        deleteUser();
    }

    /**
     * Test deleteUser() from UserHelper
     * Deletes a user from the firebase database
     */
    @Test
    public void testDeleteUser() {
        addUser();
        deleteUser();
        assertTrue("Delete user from the cloud firestore", taskIsSuccessful);
    }

    /**
     * Test getUser() from UserHelper
     * Retrieves a user from the firebase database
     */
    @Test
    public void testGetUser() {
        addUser();
        callOnApiIdl.increment();
        UserHelper.getUser(userTest.getUid()).addOnCompleteListener(result -> {
            if (result.isSuccessful() && result.getResult() != null) {
                User userResult = result.getResult().toObject(User.class);
                taskIsSuccessful = userTest.equals(userResult);
            }
            callOnApiIdl.decrement();
        });
        onIdle();
        deleteUser();
        assertTrue("Get user from the cloud firestore", taskIsSuccessful);
    }

    /**
     * Test getUsers() from UserHelper
     * Retrieves all users from the firebase database
     */
    @Test
    public void testGetUsers() {
        addUser();
        callOnApiIdl.increment();
        UserHelper.getUsers().addOnCompleteListener(result -> {
            if (result.isSuccessful() && result.getResult() != null) {
                List<User> users = result.getResult().toObjects(User.class);
                taskIsSuccessful = !users.isEmpty();
            }
            callOnApiIdl.decrement();
        });
        onIdle();
        deleteUser();
        assertTrue("Get users from database ", taskIsSuccessful);
    }

    /**
     * Test updateLunchRestaurant() from UserHelper
     * Update the restaurant where user has lunch
     */
    @Test
    public void testUpdateLunchRestaurant() {
        addUser();
        Restaurant restaurant = new Restaurant();
        restaurant.setPlaceID("updateLunchRestaurantIdTest");
        restaurant.setName("updateLunchRestaurantNameTest");
        callOnApiIdl.increment();
        UserHelper.updateLunchRestaurant(userTest.getUid(), restaurant).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            callOnApiIdl.decrement();
        });
        onIdle();
        deleteUser();
        assertTrue("Update the restaurant where to have lunch", taskIsSuccessful);
    }

    /**
     * Test addRestaurantToFavorites() from UserHelper
     * Add a restaurant to the favorites
     */
    @Test
    public void testAddRestaurantToFavorites() {
        addUser();
        callOnApiIdl.increment();
        UserHelper.addRestaurantToFavorites(userTest.getUid(), "restaurantIdFavorite")
                .addOnCompleteListener(task -> {
                            taskIsSuccessful = task.isSuccessful();
                            callOnApiIdl.decrement();
                        }
                );
        onIdle();
        deleteUser();
        assertTrue("Add a restaurant to favorites", taskIsSuccessful);
    }
}
