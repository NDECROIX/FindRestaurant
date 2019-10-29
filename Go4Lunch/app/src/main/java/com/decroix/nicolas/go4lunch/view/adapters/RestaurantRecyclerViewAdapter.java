package com.decroix.nicolas.go4lunch.view.adapters;

import android.graphics.Bitmap;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.utils.UsefulFunctions;
import com.decroix.nicolas.go4lunch.view.holders.RestaurantViewHolder;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.decroix.nicolas.go4lunch.utils.UsefulFunctions.getDistance;
import static com.decroix.nicolas.go4lunch.utils.UsefulFunctions.getOpeningHours;

public class RestaurantRecyclerViewAdapter extends RecyclerView.Adapter<RestaurantViewHolder> {

    public interface OnClickRestaurantItemListener {
        void onClickRestaurant(Place place, Bitmap bitmap);
    }

    private LatLng myLocation;
    private final OnClickRestaurantItemListener callback;
    private final List<RestaurantItem> restaurants;

    public RestaurantRecyclerViewAdapter(OnClickRestaurantItemListener callback) {
        this.restaurants = new ArrayList<>();
        this.callback = callback;
        this.myLocation = new LatLng(48.8583, 2.29448);
    }

    public void addPlace(Place place, Bitmap picture, int workmateCount) {
        RestaurantItem newRestaurant = new RestaurantItem(place, picture, workmateCount);
        restaurants.remove(newRestaurant);
        restaurants.add(0, newRestaurant);
        notifyDataSetChanged();
    }

    public void setMyLocation(@NonNull Location myLocation) {
        this.myLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
    }

    public void clearList() {
        this.restaurants.clear();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_list_view_item, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    // Glide manage this exception
    @SuppressWarnings({"unchecked"})
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        RestaurantItem restaurantItem = restaurants.get(position);
        Place place = restaurantItem.place;
        Bitmap picture = restaurantItem.bitmap;
        int count = restaurantItem.count;

        if (count > 0){
            StringBuilder sb = new StringBuilder("(").append(count).append(")");
            holder.restaurantPerson.setText(sb);
        } else{
            holder.restaurantPerson.setVisibility(View.GONE);
        }
        holder.restaurantTitle.setText(place.getName());
        holder.restaurantAddress.setText(place.getAddress());
        if (place.getOpeningHours() != null) {
            holder.restaurantOpen.setText(getOpeningHours(place.getOpeningHours()));
        }
        holder.itemView.setOnClickListener(view -> callback.onClickRestaurant(place, picture));
        if (picture != null) {
            Glide.with(holder.itemView)
                    .load(picture)
                    .transform(new MultiTransformation(new CenterCrop(), new RoundedCorners(8)))
                    .into(holder.restaurantPicture);
        }
        holder.ratingBar.setRating(UsefulFunctions.parseRating(place.getRating()));
        if (place.getLatLng() != null)
            holder.restaurantDistance.setText(getDistance(place.getLatLng(), myLocation));
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    class RestaurantItem {
        final Place place;
        final Bitmap bitmap;
        final int count;

        RestaurantItem(Place place, Bitmap bitmap, int count) {
            this.place = place;
            this.bitmap = bitmap;
            this.count = count;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!(obj instanceof RestaurantItem)){
                return false;
            }
            RestaurantItem restaurantItem = (RestaurantItem) obj;
            return Objects.equals(this.place.getId(), restaurantItem.place.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this);
        }
    }
}
