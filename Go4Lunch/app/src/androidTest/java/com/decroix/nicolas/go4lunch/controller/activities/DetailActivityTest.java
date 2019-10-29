package com.decroix.nicolas.go4lunch.controller.activities;

import androidx.fragment.app.Fragment;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.rule.ActivityTestRule;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.controller.fragments.ListViewFragment;
import com.decroix.nicolas.go4lunch.utils.ListViewFragmentRecyclerViewItemData;
import com.decroix.nicolas.go4lunch.utils.RecyclerViewIdlingResource;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onIdle;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertNotNull;

/**
 * Test DetailActivity
 */
@RunWith(AndroidJUnit4ClassRunner.class)
public class DetailActivityTest {
    @Rule
    public final ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    private MainActivity activity;

    /**
     * Setting up the necessary elements for the tests
     */
    @Before
    public void setUp() {
        activity = rule.getActivity();
        assertNotNull(activity);
        clickOnListViewFragment();
    }

    /**
     * Perform a click on the ListViewFragment icon from the bottom view
     */
    @DataPoint
    public void clickOnListViewFragment() {
        onView(withId(R.id.action_list_view)).perform(click());
        onView(withId(R.id.fragment_list_view_constraint_layout)).check(matches(isDisplayed()));
        onIdle();
    }

    /**
     * Test the click on an item of the recycler view that should open the detail activity,
     * with all details from the item
     */
    @Test
    public void clickOnItemShowDetailOnActivity() {
        RecyclerViewIdlingResource mIdl = new RecyclerViewIdlingResource("recyclerView");
        IdlingRegistry.getInstance().register(mIdl);
        Fragment fragment = activity
                .getSupportFragmentManager().findFragmentById(R.id.activity_main_frame_layout);
        if (fragment instanceof ListViewFragment) {
            ((ListViewFragment) fragment).registerOnCallBackTest(mIdl);
        }
        onIdle();
        IdlingRegistry.getInstance().unregister(mIdl);

        ListViewFragmentRecyclerViewItemData recyclerViewItemData = new ListViewFragmentRecyclerViewItemData();

        // Get data from the item at position 0
        onView(withId(R.id.fragment_list_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, recyclerViewItemData));
        // Open DetailsActivity
        onView(withId(R.id.fragment_list_recycler_view))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Check data

        onView(withText(recyclerViewItemData.restaurantTitle.getText().toString())).check(matches(isDisplayed()));

        onView(withId(R.id.detail_activity_address)).check(matches(withText((StringContains.containsString(
                recyclerViewItemData.restaurantAddress.getText().toString()))
        )));
    }


}
