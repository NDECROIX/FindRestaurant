package com.decroix.nicolas.go4lunch.view.holders;

import android.app.Activity;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.decroix.nicolas.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Allows you to link the toolbar view to the fragment
 */
public class ToolbarViewHolder {
    @BindView(R.id.main_activity_toolbar)
    public Toolbar toolbar;
    @BindView(R.id.main_activity_search_layout)
    public ConstraintLayout searchView;
    @BindView(R.id.main_activity_search_layout_rc)
    public ConstraintLayout searchViewRc;
    @BindView(R.id.search_bar_voice_icon)
    public ImageButton searchVoice;
    @BindView(R.id.search_bar_hint_icon)
    public ImageButton searchHint;
    @BindView(R.id.search_bar_edit_text)
    public EditText searchEditText;
    @BindView(R.id.search_autocomplete_recycler_view)
    public RecyclerView recyclerView;

    public ToolbarViewHolder(Activity source) {
        ButterKnife.bind(this, source);
    }
}