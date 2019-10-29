package com.decroix.nicolas.go4lunch.view.holders;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatRatingBar;
import androidx.recyclerview.widget.RecyclerView;

import com.decroix.nicolas.go4lunch.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RestaurantViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.fragment_list_item_picture)
    public ImageView restaurantPicture;
    @BindView(R.id.fragment_list_item_title)
    public TextView restaurantTitle;
    @BindView(R.id.fragment_list_item_distance)
    public TextView restaurantDistance;
    @BindView(R.id.fragment_list_item_address)
    public TextView restaurantAddress;
    @BindView(R.id.fragment_list_item_person)
    public TextView restaurantPerson;
    @BindView(R.id.fragment_list_item_open)
    public TextView restaurantOpen;
    @BindView(R.id.fragment_list_item_rating)
    public AppCompatRatingBar ratingBar;

    public RestaurantViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
