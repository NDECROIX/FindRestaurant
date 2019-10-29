package com.decroix.nicolas.go4lunch.view.holders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.decroix.nicolas.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AutocompleteViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.dialog_autocomplete_item_name)
    public TextView name;

    public AutocompleteViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
