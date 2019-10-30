package com.decroix.nicolas.go4lunch.viewmodel;

import android.content.Context;
import android.location.Location;

import androidx.lifecycle.ViewModelProviders;
import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import com.decroix.nicolas.go4lunch.controller.activities.MainActivity;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.utils.LiveDataTestUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test ShareDataViewModel
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class ShareDataViewModelTest {
    @Rule
    public final ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    private ShareDataViewModel model;
    private FirebaseUser firebaseUser;
    private Context context;

    @Before
    public void setUp() {
        MainActivity activity = rule.getActivity();
        assertNotNull(activity);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        model = ViewModelProviders.of(activity).get(ShareDataViewModel.class);
        context = ApplicationProvider.getApplicationContext();
    }

    /**
     * Test getMyUser() from ShareDataViewModel()
     * Create listener on a user from Firebase
     * @throws Exception interrupted
     */
    @Test
    @UiThreadTest
    public void getMyUserTest() throws Exception {
        User user = LiveDataTestUtil.getValue(model.getMyUser(firebaseUser.getUid()));
        assertEquals(firebaseUser.getUid(), user.getUid());
    }

    /**
     * Test getMyLocation() from ShareDataViewModel()
     * Get the current device location
     * @throws Exception interrupted
     */
    @Test
    @UiThreadTest
    public void getMyLocationTest() throws Exception {
        Location location = LiveDataTestUtil.getValue(model.getMyLocation(context, false));
        assertNotNull(location);
    }
}
