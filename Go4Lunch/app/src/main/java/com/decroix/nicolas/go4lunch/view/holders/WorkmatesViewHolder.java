package com.decroix.nicolas.go4lunch.view.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.decroix.nicolas.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmatesViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.fragment_workmates_item_picture)
    public ImageView workmatePicture;
    @BindView(R.id.fragment_workmates_item_desc)
    public TextView workmateDesc;

    public WorkmatesViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
