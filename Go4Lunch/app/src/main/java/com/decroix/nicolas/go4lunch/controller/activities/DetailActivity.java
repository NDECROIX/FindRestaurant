package com.decroix.nicolas.go4lunch.controller.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.PlacesClientHelper;
import com.decroix.nicolas.go4lunch.api.RestaurantHelper;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.base.BaseActivity;
import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.view.DetailActivityRecyclerViewAdapter;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.decroix.nicolas.go4lunch.utils.UsefulFunctions.getDistance;
import static com.decroix.nicolas.go4lunch.utils.UsefulFunctions.getOpeningHours;

public class DetailActivity extends BaseActivity {

    @BindView(R.id.activity_detail_toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_detail_picture)
    ImageView restaurantPicture;
    @BindView(R.id.detail_activity_title)
    TextView restaurantTitle;
    @BindView(R.id.detail_activity_rating)
    RatingBar restaurantRating;
    @BindView(R.id.detail_activity_address)
    TextView restaurantAddress;
    @BindView(R.id.detail_activity_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.activity_detail_fab)
    FloatingActionButton fab;

    private static final String RESTAURANT = "placeRestaurant";
    private static final String RESTAURANT_BITMAP = "restaurant_bitmap";
    private static final String MY_LOCATION = "my_location";

    private Place placeRestaurant;
    private User myUser;
    private PlacesClientHelper placesClientHelper;

    /**
     * Create an intent of this class
     *
     * @param context    Context of the application
     * @param restaurant Restaurant to be include in the bundle
     * @param bitmap     Image of the restaurant if exist else null
     * @return Intent
     */
    public static Intent newIntent(Context context, Place restaurant, @Nullable Bitmap bitmap, Location myLocation) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(RESTAURANT, restaurant);
        bundle.putParcelable(MY_LOCATION, myLocation);
        if (bitmap != null) {
            bundle.putParcelable(RESTAURANT_BITMAP, bitmap);
        }
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtras(bundle);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        configToolbar();
        getUserFromFirestore();
    }

    /**
     * Configuration of the toolbar
     */
    private void configToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    /**
     * Get data user's from firestore
     */
    private void getUserFromFirestore() {
        UserHelper.getUser(getCurrentUserID()).addOnCompleteListener(doc -> {
            if (doc.isSuccessful() && doc.getResult() != null){
                myUser = doc.getResult().toObject(User.class);
                displayPlaceDetails();
            }
        }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_get_user)));
    }

    /**
     * Display the details of the restaurant on the screen
     */
    private void displayPlaceDetails() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            this.placeRestaurant = bundle.getParcelable(RESTAURANT);
            Location myLocation = bundle.getParcelable(MY_LOCATION);
            Bitmap restaurantBitmap = bundle.getParcelable(RESTAURANT_BITMAP);

            if (getSupportActionBar() != null
                    && placeRestaurant.getOpeningHours() != null
                    && placeRestaurant.getLatLng() != null){
                getSupportActionBar()
                        .setTitle(getOpeningHours(placeRestaurant.getOpeningHours()));
            }

            restaurantTitle.setText(placeRestaurant.getName());
            restaurantAddress.setText(placeRestaurant.getAddress());
            restaurantAddress.append(" ");
            if (myLocation != null && placeRestaurant.getLatLng() != null) {
                LatLng myLatLong = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                restaurantAddress.append(getDistance(placeRestaurant.getLatLng(), myLatLong));
            }
            if (placeRestaurant.getRating() != null) {
                float rating = (float) (placeRestaurant.getRating() - 2);
                restaurantRating.setRating((rating > 0) ? rating : 0);
            }
            if (restaurantBitmap != null) {
                Glide.with(this).load(restaurantBitmap).into(restaurantPicture);
            }
            displayWorkmates();
        }
    }

    /**
     * Get and display workmate registered in the restaurant.
     */
    private void displayWorkmates() {
        RestaurantHelper.getRestaurant(placeRestaurant.getId()).addOnSuccessListener(result -> {
            if (result != null) {
                Restaurant restaurant = result.toObject(Restaurant.class);
                if (restaurant != null && restaurant.getUsers() != null) {
                    List<User> users = restaurant.getUsers();
                    updateFAB(users);
                    users.remove(myUser);
                    configRecyclerView(users);
                }
            }
        }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_get_restaurant)));
    }

    /**
     * Config the recycler view with the user list
     * @param users User list
     */
    private void configRecyclerView(List<User> users) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DetailActivityRecyclerViewAdapter(users));
    }

    /**
     * Update the color of the floating action button
     * @param users Users registered on the restaurant
     */
    private void updateFAB(List<User> users) {
        String myUid = getCurrentUserID();
        for (User user : users) {
            if (user.getUid().equals(myUid)) {
                this.fab.setImageResource(R.drawable.ic_check_circle_dark_24);
                break;
            }
        }
    }

    /**
     * Handle the click on the floating action button.
     */
    @OnClick(R.id.activity_detail_fab)
    public void onFabClick() {
        addRestaurantForLunch();
    }

    /**
     * Add the restaurant for lunch.
     * Update the profile user in the database.
     * Add the user in the users list.
     * Delete the user from the last restaurant if exist
     */
    private void addRestaurantForLunch() {
        if (!myUser.getLunchRestaurantID().equals(placeRestaurant.getId())){
            if (myUser.getLunchRestaurantID() != null && !myUser.getLunchRestaurantID().isEmpty()) {
                //Remove user from users list
                RestaurantHelper.removeUserFromList(myUser.getLunchRestaurantID(), myUser)
                        .addOnCompleteListener(task -> {
                            // Update the user profile
                            UserHelper.updateLunchRestaurant(FirebaseAuth.getInstance().getUid(), new Restaurant(placeRestaurant))
                                    .addOnFailureListener(this.onFailureListener(getString(R.string.afl_update_lunch_restaurant)));
                        }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_remove_user_from_list)));
            } else {
                UserHelper.updateLunchRestaurant(FirebaseAuth.getInstance().getUid(), new Restaurant(placeRestaurant))
                        .addOnFailureListener(this.onFailureListener(getString(R.string.afl_update_lunch_restaurant)));
            }

            // Check if the restaurant exist in the database else if create it.
            RestaurantHelper.getRestaurant(placeRestaurant.getId()).addOnCompleteListener(snapshotTask -> {
                if (snapshotTask.isSuccessful() && snapshotTask.getResult() != null) {
                    addUserInWorkmateList();
                } else {
                    Restaurant newRestaurant = new Restaurant(this.placeRestaurant);
                    RestaurantHelper.createRestaurant(newRestaurant).addOnCompleteListener(task1 -> addUserInWorkmateList());
                }
            }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_get_restaurant)));
        } else {
            showMessage(getString(R.string.restaurant_already_added));
        }
    }
    /**
     * Add the currently user in the users list
     */
    private void addUserInWorkmateList() {
        RestaurantHelper.addUserInList(placeRestaurant.getId(), myUser)
                .addOnSuccessListener(result -> {
                    fab.setImageResource(R.drawable.ic_check_circle_dark_24);
                    getUserFromFirestore();
                })
                .addOnFailureListener(this.onFailureListener(getString(R.string.afl_add_user_in_list)));
    }
}
