package com.decroix.nicolas.go4lunch.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.models.Restaurant;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AutocompleteRecyclerViewAdapter extends RecyclerView.Adapter<AutocompleteRecyclerViewAdapter.AutocompleteViewHolder>{

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

    class AutocompleteViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.dialog_autocomplete_item_name)
        TextView name;

        AutocompleteViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
