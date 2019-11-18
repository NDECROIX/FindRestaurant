package com.decroix.nicolas.go4lunch.controller.fragments;

import androidx.fragment.app.Fragment;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.controller.activities.DetailActivity;
import com.decroix.nicolas.go4lunch.controller.activities.MainActivity;
import com.decroix.nicolas.go4lunch.utils.RecyclerViewIdlingResource;
import com.decroix.nicolas.go4lunch.utils.RecyclerViewNotEmpty;
import com.decroix.nicolas.go4lunch.utils.WorkmatesFragmentRecyclerViewItemData;

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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertNotNull;

/**
 * Test ListViewFragment
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class WorkmatesFragmentTest {

    @Rule
    public final ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    private MainActivity activity;

    private final RecyclerViewIdlingResource mIdl = new RecyclerViewIdlingResource("workmateRecyclerView");

    /**
     * Setting up the necessary elements for the tests
     */
    @Before
    public void setUp() {
        activity = rule.getActivity();
        assertNotNull(activity);
        clickOnWorkmateFragment();
    }

    /**
     * Perform a click on the workmates icon from the bottom view
     */
    @DataPoint
    public void clickOnWorkmateFragment(){
        onView(withId(R.id.action_workmates)).perform(click());
        onView(withId(R.id.fragment_workmates_constraint_layout)).check(matches(isDisplayed()));
    }

    /**
     * The recycler view should not be empty
     */
    @Test
    public void checkRecyclerViewNotEmpty(){
        IdlingRegistry.getInstance().register(mIdl);
        Fragment fragment = activity
                .getSupportFragmentManager().findFragmentById(R.id.activity_main_frame_layout);
        if (fragment instanceof WorkmatesFragment) {
            ((WorkmatesFragment) fragment).registerOnCallBackTest(mIdl);
        }
        onIdle();
        IdlingRegistry.getInstance().unregister(mIdl);
        onView(withId(R.id.fragment_workmate_recycler_view)).check(new RecyclerViewNotEmpty());
    }

    /**
     * Test the click on an item of the recycler view that should open the detail activity
     * if the user had registered in a restaurant.
     */
    @Test
    public void clickOnItemShowDetailActivity() {
        Intents.init();
        IdlingRegistry.getInstance().register(mIdl);
        Fragment fragment = activity
                .getSupportFragmentManager().findFragmentById(R.id.activity_main_frame_layout);
        if (fragment instanceof WorkmatesFragment) {
            ((WorkmatesFragment) fragment).registerOnCallBackTest(mIdl);
        }
        onIdle();
        IdlingRegistry.getInstance().unregister(mIdl);

        WorkmatesFragmentRecyclerViewItemData recyclerViewItemData = new WorkmatesFragmentRecyclerViewItemData();
        // Get data from the item at position 0
        onView(withId(R.id.fragment_workmate_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, recyclerViewItemData));

        onView(withId(R.id.fragment_workmate_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        String text = recyclerViewItemData.restaurantID.getText().toString();
        if (!text.contains(activity.getString(R.string.workmates_hasnt_decided_yet))){
            intended(hasComponent(DetailActivity.class.getName()));

        }
        Intents.release();
    }
}
