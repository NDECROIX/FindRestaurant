package com.decroix.nicolas.go4lunch.utils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;

import org.junit.Assert;

import static org.junit.Assert.fail;

/**
 * Check if the recycler view is not empty
 */
public class RecyclerViewNotEmpty implements ViewAssertion {

    public RecyclerViewNotEmpty() {
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (noViewFoundException != null) {
            throw noViewFoundException;
        }

        RecyclerView recyclerView = (RecyclerView) view;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter != null) {
            Assert.assertTrue(adapter.getItemCount() > 0);
        } else {
            fail();
        }
    }
}