package com.decroix.nicolas.go4lunch.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.controller.activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static androidx.test.espresso.Espresso.onIdle;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test StorageHelper
 */
@LargeTest
@RunWith(AndroidJUnit4ClassRunner.class)
public class StorageHelperTest {

    @Rule
    public final ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    /**
     * Required for asynchronous task
     */
    private final CountingIdlingResource callOnApiIdl = new CountingIdlingResource("Call on API IdlingResource");

    /**
     * Uid of the tested image
     */
    private static final String UUID_STORAGE = "test-on-storage";

    /**
     * Referred to the task.isSuccessful() to the call on an api
     */
    private boolean taskIsSuccessful;

    private String uriTest;
    private MainActivity activity;

    /**
     * Setting up the necessary elements for the tests
     */
    @Before
    public void setup() {
        activity = rule.getActivity();
        assertNotNull(activity);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        assertNotNull(firebaseUser);
        taskIsSuccessful = false;
        uriTest = getImageUri();
        IdlingRegistry.getInstance().register(callOnApiIdl);

    }

    /**
     * Unregister on the IdlingResource
     */
    @After
    public void setDown() {
        IdlingRegistry.getInstance().unregister(callOnApiIdl);
        deleteImageUri();
    }

    /**
     * Create uri to perform test
     * @return Image path
     */
    @DataPoint
    public String getImageUri() {
        Bitmap inImage = BitmapFactory.decodeResource(activity.getResources(), R.drawable.bol_logo);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        return MediaStore.Images.Media.insertImage(activity.getContentResolver(), inImage, "imageTest", null);

    }

    /**
     * Delete image after test
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @DataPoint
    public void deleteImageUri() {
        File fileToDelete = new File(uriTest);
        fileToDelete.delete();
    }

    /**
     * Add a image to firebase storage
     */
    @DataPoint
    public void putStorageOnFirebase(){
        callOnApiIdl.increment();
        StorageHelper.putFileOnFirebaseStorage(UUID_STORAGE, Uri.parse(uriTest)).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            callOnApiIdl.decrement();
        });
        onIdle();
    }

    /**
     * Remove a image from firebase storage
     */
    @DataPoint
    public void removeFileFromFirebaseStorage(){
        callOnApiIdl.increment();
        StorageHelper.deleteFileFromFirebaseStorage(UUID_STORAGE).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful();
            callOnApiIdl.decrement();
        });
        onIdle();
    }

    /**
     *  Test putFileOnFirebaseStorage() from StorageHelper
     *  Add a image to firebase storage
     */
    @Test
    public void testPutFileOnFirebaseStorage(){
        putStorageOnFirebase();
        assertTrue("File put on storage", taskIsSuccessful);
        removeFileFromFirebaseStorage();
    }

    /**
     * Test removeFileFromFirebaseStorage()
     * Removes a file from firebase storage
     */
    @Test
    public void testRemoveFileFromFirebaseStorage(){
        putStorageOnFirebase();
        removeFileFromFirebaseStorage();
        assertTrue("Remove file from FirebaseStorage ", taskIsSuccessful);
    }

    /**
     * Test getUrlPictureFromFirebaseStorage() from StorageHelper
     * Retrieves the url of an image from firebase storage
     */
    @Test
    public void testGetUrlPictureFromFirebaseStorage(){
        putStorageOnFirebase();
        callOnApiIdl.increment();
        StorageHelper.getUrlPicture(UUID_STORAGE).addOnCompleteListener(task -> {
            taskIsSuccessful = task.isSuccessful() && task.getResult() != null;
            callOnApiIdl.decrement();
        });
        onIdle();
        assertTrue("Get url picture from FirebaseStorage ", taskIsSuccessful);
    }
}
