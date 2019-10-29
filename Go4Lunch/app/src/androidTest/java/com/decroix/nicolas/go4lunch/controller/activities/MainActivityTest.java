package com.decroix.nicolas.go4lunch.controller.activities;

import androidx.appcompat.widget.Toolbar;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.espresso.intent.Intents;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.decroix.nicolas.go4lunch.utils.NavigationViewTestHelper.childAtPosition;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Test the main activity class
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class MainActivityTest {
    @Rule
    public final ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    private MainActivity activity;
    private Toolbar toolbar;
    private FirebaseUser firebaseUser;
    private boolean restaurantExist;

    /**
     * Required for asynchronous task
     */
    private final CountingIdlingResource mIdlingRes = new CountingIdlingResource("WaitData");

    /**
     * Setting up the necessary elements for the tests
     */
    @Before
    public void setUp() {
        activity = rule.getActivity();
        assertNotNull(activity);
        toolbar = activity.findViewById(R.id.main_activity_toolbar);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        assertNotNull(firebaseAuth);
        firebaseUser = firebaseAuth.getCurrentUser();
        assertNotNull(firebaseUser);
    }

    /**
     * Retrieves the current user from the firebase database and check if he has
     * a restaurant to lunch
     */
    @DataPoint
    public void checkIfRestaurantExist() {
        IdlingRegistry.getInstance().register(mIdlingRes);
        mIdlingRes.increment();
        UserHelper.getUser(firebaseUser.getUid()).addOnSuccessListener(doc -> {
            User user = doc.toObject(User.class);
            if (user != null && user.getLunchRestaurantID() != null && !user.getLunchRestaurantID().isEmpty()) {
                restaurantExist = true;
            }
            mIdlingRes.decrement();
        });
        onIdle();
        IdlingRegistry.getInstance().unregister(mIdlingRes);
    }

    /**
     * Check if the bottom view is displayed
     */
    @Test
    public void bottomViewIsDisplay() {
        onView(withId(R.id.activity_main_bottom_navigation)).check(matches(isDisplayed()));
        onView(withId(R.id.action_map_view)).check(matches(isDisplayed()));
        onView(withId(R.id.action_list_view)).check(matches(isDisplayed()));
        onView(withId(R.id.action_workmates)).check(matches(isDisplayed()));
        onView(withId(R.id.action_chat)).check(matches(isDisplayed()));
    }

    /**
     * Perform a click on the navigation icon should open the navigation view
     */
    @Test
    public void showNavigationView() {
        onView(withContentDescription(String.valueOf(toolbar.getNavigationContentDescription())))
                .perform(click());
        onView(withId(R.id.main_activity_navigation_view)).check(matches(isDisplayed()));
    }

    /**
     * Perform a click on the search icon should display the search toolbar
     */
    @Test
    public void showSearchView() {
        onView(withId(R.id.menu_activity_main_search))
                .perform(click());
        onView(withId(R.id.search_bar_edit_text)).check(matches(isDisplayed()));
        onView(withId(R.id.search_bar_hint_icon)).check(matches(isDisplayed()));
        onView(withId(R.id.search_bar_voice_icon)).check(matches(isDisplayed()));
    }

    /**
     * Check if the map fragment is displayed
     */
    @Test
    public void mapFragmentIsDisplay() {
        onView(withId(R.id.fragment_map_view)).check(matches(isDisplayed()));
    }

    /**
     * Perform a click on the ViewList icon to display
     * the fragment of the list of restaurants
     */
    @Test
    public void clickOnViewList() {
        onView(withId(R.id.action_list_view)).perform(click());
        onView(withId(R.id.fragment_list_view_constraint_layout)).check(matches(isDisplayed()));
    }

    /**
     * Perform a click on the WorkmateList icon to display the fragment
     * of the list of workmates
     */
    @Test
    public void clickOnWorkmatesList() {
        onView(withId(R.id.action_workmates)).perform(click());
        onView(withId(R.id.fragment_workmates_constraint_layout)).check(matches(isDisplayed()));
    }

    /**
     * Perform a click on the Chat icon to display the chat fragment
     */
    @Test
    public void clickOnChat() {
        onView(withId(R.id.action_chat)).perform(click());
        onView(withId(R.id.fragment_chat_constraint_layout)).check(matches(isDisplayed()));
    }

    /**
     * Perform a click on the settings icon to display the settings activity
     */
    @Test
    public void clickOnSettings() {
        Intents.init();
        onView(withContentDescription(String.valueOf(toolbar.getNavigationContentDescription())))
                .perform(click());
        ViewInteraction navigationMenuItemView = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.main_activity_navigation_view),
                                        0)),
                        2),
                        isDisplayed()));
        navigationMenuItemView.perform(click());

        intended(hasComponent(SettingsActivity.class.getName()));
        Intents.release();
    }

    /**
     * Click on the YourLunch icon to display the detail activity.
     */
    @Test
    public void clickOnYourLunch() {
        checkIfRestaurantExist();
        Intents.init();
        onView(withContentDescription(String.valueOf(toolbar.getNavigationContentDescription())))
                .perform(click());
        ViewInteraction navigationMenuItemView = onView(
                allOf(childAtPosition(
                        allOf(withId(R.id.design_navigation_view),
                                childAtPosition(
                                        withId(R.id.main_activity_navigation_view),
                                        0)),
                        1),
                        isDisplayed()));
        navigationMenuItemView.perform(click());
        onIdle();
        if (restaurantExist) {
            intended(hasComponent(DetailActivity.class.getName()));
        } else {
            onView(withText(R.string.not_yet_chosen))
                    .inRoot(withDecorView(not(is(activity.getWindow().getDecorView())))).check(matches(isDisplayed()));
        }
        Intents.release();
    }
}
