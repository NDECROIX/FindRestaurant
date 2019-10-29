package com.decroix.nicolas.go4lunch.controller.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.api.PlacesClientHelper;
import com.decroix.nicolas.go4lunch.api.UserHelper;
import com.decroix.nicolas.go4lunch.base.BaseActivity;
import com.decroix.nicolas.go4lunch.controller.fragments.ChatFragment;
import com.decroix.nicolas.go4lunch.controller.fragments.ListViewFragment;
import com.decroix.nicolas.go4lunch.controller.fragments.MapViewFragment;
import com.decroix.nicolas.go4lunch.controller.fragments.WorkmatesFragment;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.receiver.AlarmReceiver;
import com.decroix.nicolas.go4lunch.view.AutocompleteRecyclerViewAdapter;
import com.decroix.nicolas.go4lunch.view.RestaurantRecyclerViewAdapter;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
MapViewFragment.MapViewFragmentInterface, RestaurantRecyclerViewAdapter.OnClickRestaurantItemListener {

    //------------
    // VIEW
    //------------

    @BindView(R.id.activity_main_bottom_navigation)
    BottomNavigationView bottomNavigationView;
    @BindView(R.id.main_activity_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.main_activity_navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.main_activity_search_layout)
    ConstraintLayout mSearchView;

    private HeaderViewHolder mHeaderViewHolder;

    //------------
    // FRAGMENTS
    //------------

    private final FragmentManager fragmentManager = getSupportFragmentManager();
    private final MapViewFragment mapViewFragment = new MapViewFragment();
    private final ListViewFragment listViewFragment = new ListViewFragment();
    private final WorkmatesFragment workmatesFragment = new WorkmatesFragment();
    private final ChatFragment chatFragment = new ChatFragment();
    private Fragment activeFragment = mapViewFragment;

    public Location mLastKnownLocation;
    private boolean notificationCaller;

    /**
     * Create a intent of this activity
     *
     * @param context Application context
     * @return The intent
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (!isCurrentUserLogged()) {
            startActivity(AuthActivity.newIntent(this));
        } else {
            startFragment();
            configToolbar();
            configBottomView();
            configDrawerLayout();
            configNavigationView();
        }
    }

    /**
     * Start the first fragment "MapViewFragment"
     */
    private void startFragment() {
        fragmentManager.beginTransaction()
                .add(R.id.activity_main_frame_layout, activeFragment)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

    /**
     * Displays the user data on the NavDrawer
     */
    private void updateUI() {
        if (!isCurrentUserLogged()) {
            startActivity(AuthActivity.newIntent(this));
        } else {
            Objects.requireNonNull(getCurrentUser(), getString(R.string.rnn_user_must_not_be_null));
            Glide.with(this)
                    .load(getCurrentUser().getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(mHeaderViewHolder.mAvatar);
            mHeaderViewHolder.mName.setText(getCurrentUser().getDisplayName());
            mHeaderViewHolder.mEmail.setText(getCurrentUser().getEmail());
        }
        if (Objects.equals(getIntent().getStringExtra(EXTRA_CALLER), AlarmReceiver.class.getName()) && !notificationCaller) {
            displayUsersRestaurant();
            notificationCaller = true;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_activity_main_search) {
            searchItemSelected();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * performs an action according to the active fragment.
     */
    private void searchItemSelected() {
        if (activeFragment == workmatesFragment) {
            ((WorkmatesFragment) activeFragment).configSearchToolbar(null, null, ((WorkmatesFragment) activeFragment));
        } else if (activeFragment == listViewFragment) {
            ((ListViewFragment) activeFragment).configSearchToolbar((AutocompleteRecyclerViewAdapter.onClickAutocompleteResultListener) activeFragment, mLastKnownLocation, null);
        } else {
            ((MapViewFragment) activeFragment).configSearchToolbar(((AutocompleteRecyclerViewAdapter.onClickAutocompleteResultListener) activeFragment), mLastKnownLocation, null);
        }
    }

    /**
     * Configures the toolbar
     */
    private void configToolbar() {
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.toolbar_title_hungry));
        }
    }

    /**
     * Configures the Drawer layout
     */
    private void configDrawerLayout() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    /**
     * Configures the navigation view
     */
    private void configNavigationView() {
        mNavigationView.setNavigationItemSelectedListener(this);
        View header = mNavigationView.getHeaderView(0);
        mHeaderViewHolder = new HeaderViewHolder(header);
    }

    /**
     * Configures the bottom view
     */
    private void configBottomView() {
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> updateMainFragment(item.getItemId()));
    }

    /**
     * Change the activeFragment when the user clicks on the bottom navigation view
     *
     * @param itemId Item ID
     * @return true
     */
    private boolean updateMainFragment(int itemId) {
        switch (itemId) {
            case R.id.action_map_view:
                activeFragment = mapViewFragment;
                break;
            case R.id.action_list_view:
                activeFragment = listViewFragment;
                break;
            case R.id.action_workmates:
                activeFragment = workmatesFragment;
                break;
            case R.id.action_chat:
                activeFragment = chatFragment;
                break;
            default:
                return false;
        }
        showFragment();
        return true;
    }

    /**
     * Updates the active fragment
     */
    private void showFragment() {
        Objects.requireNonNull(getSupportActionBar(), getString(R.string.rnn_action_bar_must_not_be_null));
        if (activeFragment == workmatesFragment) {
            getSupportActionBar().setTitle(getString(R.string.toolbar_title_workmates));
        } else if (activeFragment == chatFragment) {
            getSupportActionBar().setTitle(getString(R.string.toolbar_title_chat));
        } else {
            getSupportActionBar().setTitle(getString(R.string.toolbar_title_hungry));
        }
        fragmentManager.beginTransaction()
                .replace(R.id.activity_main_frame_layout, activeFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.activity_main_drawer_lunch:
                displayUsersRestaurant();
                break;
            case R.id.activity_main_drawer_settings:
                // start setting activity;
                break;
            case R.id.activity_main_drawer_logout:
                logout();
                break;
            default:
                break;
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Logout from firebase and leave the current activity
     */
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(AuthActivity.newIntent(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        activeFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Get and start the detail activity with the restaurant of the user
     */
    private void displayUsersRestaurant() {
        PlacesClientHelper placesClientHelper = new PlacesClientHelper(this);
        // Get user data from firestore
        UserHelper.getUser(getCurrentUserID()).addOnSuccessListener(resultUser -> {
            if (resultUser != null) {
                User user = resultUser.toObject(User.class);
                if  (user != null && user.getLunchRestaurantID() != null){
                    // Get place detail from the Google place api
                    placesClientHelper.getPlaceDetails(user.getLunchRestaurantID())
                            .addOnSuccessListener(place -> {
                                if (place != null) {
                                    Place myPlace = place.getPlace();
                                    if (myPlace.getPhotoMetadatas() != null) {
                                        // Get the restaurant picture if exist
                                        placesClientHelper.getBitmapFromPlace(myPlace.getPhotoMetadatas().get(0))
                                                .addOnSuccessListener(fetchPhotoResponse -> {
                                                    if (fetchPhotoResponse != null)
                                                        startDetailActivity(myPlace, fetchPhotoResponse.getBitmap());
                                                }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_fetch_photo)));
                                    }else {
                                        startDetailActivity(myPlace, null);
                                    }
                                }
                            }).addOnFailureListener(this.onFailureListener(getString(R.string.afl_fetch_place)));
                } else {
                    showMessage(getString(R.string.not_yet_chosen));
                }
            }
        });
    }

    /**
     * Start the activity DetailActivity
     * @param place restaurant
     * @param bitmap restaurant's picture
     */
    private void startDetailActivity(Place place, Bitmap bitmap){
        startActivity(DetailActivity.newIntent(this, place, bitmap, mLastKnownLocation));
    }

    @Override
    public void onClickRestaurantMarker(Place restaurant, Bitmap bitmap) {
        startDetailActivity(restaurant, bitmap);
    }

    @Override
    public void onClickRestaurant(Place place, Bitmap bitmap) {
        startDetailActivity(place, bitmap);
    }

    /**
     * Update the last known location
     * @param mLastKnownLocation actual location
     */
    @Override
    public void updateLastKnowLocation(Location mLastKnownLocation) {
        this.mLastKnownLocation = mLastKnownLocation;
    }

    class HeaderViewHolder {
        @BindView(R.id.main_activity_nav_header_avatar)
        protected ImageView mAvatar;
        @BindView(R.id.main_activity_nav_header_name)
        protected TextView mName;
        @BindView(R.id.main_activity_nav_header_email)
        protected TextView mEmail;

        HeaderViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
