package com.decroix.nicolas.go4lunch.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.models.Restaurant;
import com.decroix.nicolas.go4lunch.view.holders.AutocompleteViewHolder;

import java.util.ArrayList;
import java.util.List;

public class AutocompleteRecyclerViewAdapter extends RecyclerView.Adapter<AutocompleteViewHolder> {

    public interface onClickAutocompleteResultListener{
        void onClickAutocompleteResult(Restaurant restaurant);
    }

    private final onClickAutocompleteResultListener callback;
    private final List<Restaurant> restaurants;

    public AutocompleteRecyclerViewAdapter(onClickAutocompleteResultListener callback, List<Restaurant> restaurants) {
        this.callback = callback;
        this.restaurants = new ArrayList<>();
        this.restaurants.addAll(restaurants);
    }

    public void updateList(List<Restaurant> restaurants){
        this.restaurants.clear();
        this.restaurants.addAll(restaurants);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AutocompleteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_main_toolbar_search_autocomplete_item,
                parent, false);
        return new AutocompleteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AutocompleteViewHolder holder, int position) {
        Restaurant restaurant = restaurants.get(position);
        holder.name.setText(restaurant.getName());
        holder.itemView.setOnClickListener(view -> callback.onClickAutocompleteResult(restaurant));
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }
}
