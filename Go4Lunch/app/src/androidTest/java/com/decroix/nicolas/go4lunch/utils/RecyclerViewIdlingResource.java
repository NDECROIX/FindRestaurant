package com.decroix.nicolas.go4lunch.utils;

import androidx.test.espresso.IdlingResource;

import com.decroix.nicolas.go4lunch.test.TestRecyclerView;

/**
 * This class is waiting for the first element of the recycler's view
 */
public class RecyclerViewIdlingResource implements IdlingResource, TestRecyclerView {

    private final String name;
    private boolean isIdle;

    private volatile ResourceCallback resourceCallback;

    public RecyclerViewIdlingResource(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isIdleNow() {
        return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.resourceCallback = callback;
    }

    @Override
    public void recyclerViewHaveData() {
        if (resourceCallback == null){
            return;
        }
        isIdle = true;
        resourceCallback.onTransitionToIdle();
    }
}
