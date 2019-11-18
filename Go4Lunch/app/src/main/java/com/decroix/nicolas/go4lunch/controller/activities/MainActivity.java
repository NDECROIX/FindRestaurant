package com.decroix.nicolas.go4lunch.controller.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decroix.nicolas.go4lunch.BuildConfig;
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
import com.decroix.nicolas.go4lunch.view.adapters.AutocompleteRecyclerViewAdapter;
import com.decroix.nicolas.go4lunch.view.holders.HeaderViewHolder;
import com.decroix.nicolas.go4lunch.viewmodel.ShareDataViewModel;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener,
        DetailActivity.StartDetailActivity {

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

    // Fragments of the main activity
    private final MapViewFragment mapViewFragment = new MapViewFragment();
    private final ListViewFragment listViewFragment = new ListViewFragment();
    private final WorkmatesFragment workmatesFragment = new WorkmatesFragment();
    private final ChatFragment chatFragment = new ChatFragment();
    private Fragment activeFragment = mapViewFragment;

    /**
     * Last known location of the device
     */
    private Location mLastKnownLocation;

    /**
     * True if this activity was opened by the notification
     */
    private boolean notificationCaller;

    private User myUser;

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
            startActivity(AuthActivity.newIntent(this, MainActivity.class.getName()));
        } else {
            configView();
        }
    }

    /**
     * Configures all views
     */
    private void configView() {
        startFragment();
        configViewModel();
        configToolbar();
        configBottomView();
        configDrawerLayout();
        configNavigationView();
    }

    /**
     * Handles ViewModel data
     */
    private void configViewModel() {
        ShareDataViewModel model = ViewModelProviders.of(this).get(ShareDataViewModel.class);
        model.getMyUser(getCurrentUserID()).observe(this, user -> myUser = user);
        model.getMyLocation(this, false).observe(this, location -> mLastKnownLocation = location);
    }

    /**
     * Start the first fragment "MapViewFragment"
     */
    private void startFragment() {
        this.getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_main_frame_layout, activeFragment)
                .commitNow();
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

    @Override
    protected void onStart() {
        super.onStart();
        updateUI();
    }

    /**
     * Displays user data on the NavDrawer or opens DetailActivity if the activity
     * has been opened by a notification
     */
    private void updateUI() {
        getSupportFragmentManager().beginTransaction().attach(activeFragment).commitNow();
        if (!isCurrentUserLogged()) {
            startActivity(AuthActivity.newIntent(this, MainActivity.class.getName()));
        }
        if (Objects.equals(getIntent().getStringExtra(EXTRA_CALLER), AlarmReceiver.class.getName()) && !notificationCaller) {
            if (myUser == null) {
                UserHelper.getUser(getCurrentUserID()).addOnSuccessListener(task -> {
                    myUser = (task != null) ? task.toObject(User.class) : null;
                    notificationCaller = true;
                    displayUsersRestaurant();
                });
            }
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
     * Performs an action according to the active fragment.
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
        HeaderViewHolder mHeaderViewHolder = new HeaderViewHolder(header);
        Objects.requireNonNull(getCurrentUser(), getString(R.string.rnn_user_must_not_be_null));
        Glide.with(this)
                .load(getCurrentUser().getPhotoUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(mHeaderViewHolder.mAvatar);
        mHeaderViewHolder.mName.setText(getCurrentUser().getDisplayName());
        mHeaderViewHolder.mEmail.setText(getCurrentUser().getEmail());
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
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_frame_layout, activeFragment)
                .addToBackStack(null)
                .commit();
        updateToolbarTitle();
    }

    /**
     * Update the toolbar title
     */
    private void updateToolbarTitle() {
        Objects.requireNonNull(getSupportActionBar(), getString(R.string.rnn_action_bar_must_not_be_null));
        if (activeFragment == workmatesFragment) {
            getSupportActionBar().setTitle(getString(R.string.toolbar_title_workmates));
        } else if (activeFragment == chatFragment) {
            getSupportActionBar().setTitle(getString(R.string.toolbar_title_chat));
        } else {
            getSupportActionBar().setTitle(getString(R.string.toolbar_title_hungry));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.activity_main_drawer_lunch:
                displayUsersRestaurant();
                break;
            case R.id.activity_main_drawer_settings:
                getSupportFragmentManager().beginTransaction().detach(activeFragment).commitNow();
                startActivity(SettingsActivity.newIntent(this));
                break;
            case R.id.activity_main_drawer_logout:
                getSupportFragmentManager().beginTransaction().detach(activeFragment).commitNow();
                logout();
                break;
            default:
                break;
        }
        this.mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Logout from Firebase and leave the current activity
     */
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        startActivity(AuthActivity.newIntent(this, MainActivity.class.getName()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        activeFragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Get and start the detail activity with the restaurant of the user
     */
    private void displayUsersRestaurant() {
        Places.initialize(this, BuildConfig.ApiKey);
        PlacesClient placesClient = Places.createClient(this);
        if (myUser != null && myUser.getLunchRestaurantID() != null && !myUser.getLunchRestaurantID().isEmpty()) {
            PlacesClientHelper.startDetailActivity(placesClient, myUser.getLunchRestaurantID(), this);
        } else {
            showMessage(getString(R.string.not_yet_chosen));
        }
    }

    /**
     * Start the activity DetailActivity
     *
     * @param place  restaurant
     * @param bitmap restaurant's picture
     */
    @Override
    public void startDetailActivity(Place place, Bitmap bitmap) {
        getSupportFragmentManager().beginTransaction().detach(activeFragment).commitNow();
        startActivity(DetailActivity.newIntent(this, place, bitmap, mLastKnownLocation));
    }
}
