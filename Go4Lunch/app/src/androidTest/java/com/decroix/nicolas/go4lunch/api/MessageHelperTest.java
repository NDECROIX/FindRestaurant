package com.decroix.nicolas.go4lunch.api;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import com.decroix.nicolas.go4lunch.controller.activities.MainActivity;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

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
import static org.junit.Assert.fail;

/**
 * Test MessageHelper
 */
@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class MessageHelperTest {
    @Rule
    public final ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, false, true);

    /**
     * Required for asynchronous task
     */
    private final CountingIdlingResource callOnApiIdl = new CountingIdlingResource("Call on API IdlingResource");

    /**
     * Referred to the task.isSuccessful() to the call on an api
     */
    private boolean taskIsSuccessful;

    /**
     * User to perform test
     */
    private User userTest;

    /**
     * List of message ids to be deleted
     */
    private List<String> messagesTest;

    /**
     * Id of the message created by a test
     */
    private String messageCreateForTest;

    /**
     * Current user
     */
    private FirebaseUser firebaseUser;

    /**
     * Setting up the necessary elements for the tests
     */
    @Before
    public void setup() {
        MainActivity activity = rule.getActivity();
        assertNotNull(activity);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        assertNotNull(firebaseUser);
        assertNotNull("You need your own photo to do a test", firebaseUser.getPhotoUrl());
        taskIsSuccessful = false;
        messagesTest = new ArrayList<>();
        userTest = createUserTest();
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
     * Add a message on firebase database
     */
    @DataPoint
    public void addMessageToTheChat() {
        callOnApiIdl.increment();
        MessageHelper.createMessage("messageTest", userTest).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            if (task.getResult() != null) {
                messageCreateForTest = task.getResult().getId();
            }
            callOnApiIdl.decrement();
        });
        onIdle();
    }

    /**
     * Retrieves all messages from the userTest
     */
    @DataPoint
    public void getMessagesFromUserSender() {
        callOnApiIdl.increment();
        MessageHelper.getMessageFromUserSender(userTest.getUid()).get().addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            if (taskIsSuccessful && task.getResult() != null) {
                for (DocumentSnapshot documentSnapshot : task.getResult().getDocuments()) {
                    messagesTest.add(documentSnapshot.getId());
                }
            }
            callOnApiIdl.decrement();
        });
        onIdle();
    }

    /**
     * Deletes all message passed as parameter
     * @param messagesTest List of message ids
     */
    @DataPoint
    public void deleteMessagesFromFirebase(List<String> messagesTest) {
        getMessagesFromUserSender();
        taskIsSuccessful = true; // all call take the same boolean for return false if one fail
        for (String messageID : messagesTest) {
            callOnApiIdl.increment();
            MessageHelper.deleteMessage(messageID).addOnCompleteListener(task -> {
                taskIsSuccessful = task.isSuccessful() && taskIsSuccessful;
                callOnApiIdl.decrement();
            });
        }
        onIdle();
    }

    /**
     * Test createMessage() from MessageHelper
     * Creates a message on firebase database
     */
    @Test
    public void testCreateMessage() {
        addMessageToTheChat();
        assertTrue("Create message.", taskIsSuccessful);
        deleteMessagesFromFirebase(Collections.singletonList(messageCreateForTest));
    }

    /**
     * Test createMessageWitheImage() from MessageHelper
     * Creates a message with an urlPicture on firebase database
     */
    @Test
    public void testCreateMessageWitheImage() {
        if (firebaseUser.getPhotoUrl() == null) {
            fail("No photo for the test.");
            return;
        }
        callOnApiIdl.increment();
        MessageHelper.createMessageWithImageForChat(firebaseUser.getPhotoUrl().toString(), "messageTest", userTest).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            if (task.getResult() != null) {
                messageCreateForTest = task.getResult().getId();
            }
            callOnApiIdl.decrement();
        });
        onIdle();
        assertTrue("Create message with image.", taskIsSuccessful);
        deleteMessagesFromFirebase(Collections.singletonList(messageCreateForTest));
    }

    /**
     * Test getAllMessageFromFirebase() from MessageHelper
     * Retrieves all messages from firebase database
     */
    @Test
    public void testGetAllMessageFromFirebase() {
        callOnApiIdl.increment();
        MessageHelper.getAllMessageForChat().get().addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            callOnApiIdl.decrement();
        });
        onIdle();
        assertTrue("Get all message from firestore", taskIsSuccessful);
    }

    /**
     * Test getMessageFromUserSender() from MessageHelper
     * Retrieves all messages from the specified user
     */
    @Test
    public void testGetMessageFromUserSender() {
        getMessagesFromUserSender();
        assertTrue("Get message from user sender", taskIsSuccessful);
    }

    /**
     * Test deleteMessageFromFirebase() from MessageHelper
     * Deletes all message from the specified user
     */
    @Test
    public void testDeleteMessageFromFirebase() {
        getMessagesFromUserSender();
        deleteMessagesFromFirebase(messagesTest);
        assertTrue("Delete messages from firestore", taskIsSuccessful);
    }
}
