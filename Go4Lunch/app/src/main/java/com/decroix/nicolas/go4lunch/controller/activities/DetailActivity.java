package com.decroix.nicolas.go4lunch.controller.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.base.BaseActivity;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import javax.annotation.Nullable;

import butterknife.BindView;

public class DetailActivity extends BaseActivity {

    private static final String RESTAURANT = "placeRestaurant";
    private static final String RESTAURANT_BITMAP = "restaurant_bitmap";
    private static final String MY_LOCATION = "my_location";

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
    }
}
