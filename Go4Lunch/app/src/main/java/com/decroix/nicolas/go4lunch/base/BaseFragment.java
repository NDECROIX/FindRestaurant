package com.decroix.nicolas.go4lunch.base;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public abstract class BaseFragment extends Fragment {

    /**
     * Array of all data field return by the find current place request
     */
    protected static final List<Place.Field> PLACES_FIELDS = Arrays.asList(Place.Field.ID, Place.Field.TYPES, Place.Field.LAT_LNG);

    /**
     * Array of all data field return by the fetch place request
     * No more Cost.
     * ADDRESS, ID, LAT_LNG, NAME, OPENING_HOURS, PHOTO_METADATAS, PLUS_CODE, TYPES, UTC_OFFSET, VIEWPORT.
     */
    protected static final List<Place.Field> PLACE_FIELDS = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS,
            Place.Field.OPENING_HOURS, Place.Field.RATING, Place.Field.PHOTO_METADATAS, Place.Field.LAT_LNG,
            Place.Field.TYPES);

    private Context context;

    /**
     * Get the current user logged
     *
     * @return Firebase user
     */
    @Nullable
    private FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * Get the current user ID
     *
     * @return Firebase user ID
     */
    @Nullable
    protected String getCurrentUserID() {
        if (getCurrentUser() != null) {
            return getCurrentUser().getUid();
        }
        return null;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    protected Context getFragmentContext() {
        return context;
    }

    /**
     * Handled the failure from a Task
     *
     * @return On failure listener
     */
    protected OnFailureListener onFailureListener(String message) {
        return e -> Toast.makeText(getContext(),
                message, Toast.LENGTH_LONG).show();
    }

    /**
     * Display the message passed as a parameter
     *
     * @param message Message that we show to the user
     */
    protected void showMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

}
