package com.decroix.nicolas.go4lunch.base;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.decroix.nicolas.go4lunch.BuildConfig;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.decroix.nicolas.go4lunch.view.adapters.AutocompleteRecyclerViewAdapter;
import com.decroix.nicolas.go4lunch.view.holders.ToolbarViewHolder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

/**
 * Class that handle the search view
 */
public abstract class ToolbarAutocomplete extends BaseFragment {

    private static final int REQ_CODE_SPEECH_INPUT = 959;

    private ToolbarViewHolder toolbarViewHolder;
    private InputMethodManager inputMethodManager;
    private Location mLastKnownLocation;
    private PlacesClient placesClient;
    private TextWatcher textWatcher;
    private AutocompleteRecyclerViewAdapter.onClickAutocompleteResultListener callback;
    private AutocompleteRecyclerViewAdapter adapter;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbarViewHolder = new ToolbarViewHolder(getActivity());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Call by the main activity this function update the toolbar for create a searchView
     *
     * @param callback           Handle the click on restaurant name
     * @param mLastKnownLocation the last known location of the device
     * @param textWatcher        Handle text in the search view
     */
    public void configSearchToolbar(@Nullable AutocompleteRecyclerViewAdapter.onClickAutocompleteResultListener callback, Location mLastKnownLocation, @Nullable TextWatcher textWatcher) {
        this.textWatcher = textWatcher;
        this.callback = callback;
        this.mLastKnownLocation = mLastKnownLocation;
        if (textWatcher == null) {
            Places.initialize(getFragmentContext(), BuildConfig.ApiKey);
            placesClient = Places.createClient(getFragmentContext());
            configRecyclerView(getFragmentContext());
            createTextWatcher();
        }
        showToolbar(false);
        configListener();
    }

    /**
     * Show or hide the toolbar for let see the searchView
     *
     * @param visibility If (true) show toolbar
     */
    protected void showToolbar(boolean visibility) {
        toolbarViewHolder.toolbar.getMenu().findItem(R.id.menu_activity_main_search).setVisible(visibility);
        if (visibility) {
            toolbarViewHolder.toolbar.setNavigationIcon(R.drawable.ic_menu_24);
            toolbarViewHolder.searchView.setVisibility(View.GONE);
            toolbarViewHolder.searchViewRc.setVisibility(View.GONE);
        } else {
            toolbarViewHolder.toolbar.setNavigationIcon(null);
            toolbarViewHolder.searchView.setVisibility(View.VISIBLE);
        }
        if (!visibility && mLastKnownLocation != null) {
            toolbarViewHolder.searchEditText.setHint(R.string.search_view_hint_restaurants);
            toolbarViewHolder.searchViewRc.setVisibility(View.VISIBLE);
            toolbarViewHolder.searchViewRc.bringToFront();
            toolbarViewHolder.toolbar.setPadding(0, 0, 0, 0);
            toolbarViewHolder.searchView.setBackground(ContextCompat.getDrawable(getFragmentContext(), R.drawable.background_autocomplete_search_toolbar));
        } else if (!visibility){
            toolbarViewHolder.searchEditText.setHint(R.string.search_view_hint_workmates);
            toolbarViewHolder.toolbar.setPadding(0, 0, 0, 10);
            toolbarViewHolder.searchView.setBackground(ContextCompat.getDrawable(getFragmentContext(), R.drawable.background_search_toolbar));
        }
    }

    /**
     * Configures the recycler view to display the names of restaurants
     *
     * @param context Activity context
     */
    private void configRecyclerView(Context context) {
        toolbarViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(context));
        toolbarViewHolder.recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        adapter = new AutocompleteRecyclerViewAdapter(callback, new ArrayList<>());
        toolbarViewHolder.recyclerView.setAdapter(adapter);
    }

    /**
     * Configures listeners to be able to handle the searchView
     */
    private void configListener() {
        inputMethodManager = (InputMethodManager) toolbarViewHolder.searchEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        toolbarViewHolder.searchEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                toolbarViewHolder.searchEditText.setText("");
                toolbarViewHolder.searchEditText.clearFocus();
                showToolbar(true);
                return true;
            }
            return false;
        });
        toolbarViewHolder.searchEditText.setOnFocusChangeListener((view, focus) -> {
            if (!focus) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                showToolbar(true);
            } else {
                inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        });
        toolbarViewHolder.searchEditText.addTextChangedListener(textWatcher);
        toolbarViewHolder.searchVoice.setOnClickListener(view -> promptSpeechInput());
        toolbarViewHolder.searchEditText.requestFocus();
        toolbarViewHolder.searchViewRc.setOnClickListener(c -> {
            toolbarViewHolder.searchEditText.setText("");
            toolbarViewHolder.searchEditText.clearFocus();
        });
    }

    /**
     * Create a text watcher for google place autocomplete
     */
    private void createTextWatcher() {
        FindAutocompletePredictionsRequest.Builder requestBuilder = createPredictionsRequest();
        this.textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                FindAutocompletePredictionsRequest request = requestBuilder
                        .setQuery(String.valueOf(charSequence))
                        .build();

                placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                    List<AutocompletePrediction> autocompletePredictions = response.getAutocompletePredictions();
                    List<Restaurant> restaurants = new ArrayList<>();
                    for (int c = 0; c < autocompletePredictions.size(); c++) {
                        if (autocompletePredictions.get(c).getPlaceTypes().contains(Place.Type.RESTAURANT)) {
                            Restaurant restaurant = new Restaurant();
                            restaurant.setName(autocompletePredictions.get(c).getPrimaryText(null).toString());
                            restaurant.setPlaceID(autocompletePredictions.get(c).getPlaceId());
                            restaurants.add(restaurant);
                        }
                    }
                    adapter.updateList(restaurants);
                });
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };
    }

    /**
     * Create a predictions request for the text watcher
     *
     * @return Find autocomplete predictions request
     */
    private FindAutocompletePredictionsRequest.Builder createPredictionsRequest() {
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        return FindAutocompletePredictionsRequest.builder()
                .setLocationRestriction(RectangularBounds.newInstance(
                        new LatLng(mLastKnownLocation.getLatitude() - 1, mLastKnownLocation.getLongitude() - 0.1),
                        new LatLng(mLastKnownLocation.getLatitude() + 1, mLastKnownLocation.getLongitude() + 0.1)))
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setSessionToken(token);
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                R.string.search_view_speech_prompt);
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            showMessage(getString(R.string.search_view_speech_not_supported));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Handle speech result
        if (requestCode == REQ_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null) {
                    toolbarViewHolder.searchEditText.setText(result.get(0));
                }
            }
        }
    }
}