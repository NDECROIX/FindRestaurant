package com.decroix.nicolas.go4lunch.utils;

import android.view.View;
import android.widget.TextView;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import com.decroix.nicolas.go4lunch.R;

import org.hamcrest.Matcher;

/**
 * Obtain item data from the recycler's view
 */
public class WorkmatesFragmentRecyclerViewItemData implements ViewAction {

    public TextView restaurantID;

    @Override
    public Matcher<View> getConstraints() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void perform(UiController uiController, View view) {
        restaurantID = view.findViewById(R.id.fragment_workmates_item_desc);
    }
}
