package com.decroix.nicolas.go4lunch.controller.activities;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.controller.fragments.SettingsFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.settings_toolbar)
    Toolbar settingsToolbar;

    /**
     * Create an intent of this activity
     * @param context Context to launch this activity
     * @return SettingsActivity intent
     */
    public static Intent newIntent(Context context){
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.settings, new SettingsFragment())
                .commit();

        configToolbar();
    }

    private void configToolbar() {
        setSupportActionBar(settingsToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.toolbar_title_settings));
        }
    }
}
