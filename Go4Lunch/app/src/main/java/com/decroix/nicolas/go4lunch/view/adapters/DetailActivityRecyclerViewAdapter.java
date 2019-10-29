package com.decroix.nicolas.go4lunch.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.models.User;
import com.decroix.nicolas.go4lunch.view.holders.DetailActivityViewHolder;

import java.util.List;

public class DetailActivityRecyclerViewAdapter extends RecyclerView.Adapter<DetailActivityViewHolder> {

    private final List<User> users;

    public DetailActivityRecyclerViewAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public DetailActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_detail_item, parent, false);
        return new DetailActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailActivityViewHolder holder, int position) {
        User user = users.get(position);

        if (user.getUrlPicture() != null)
        Glide.with(holder.itemView)
                .load(user.getUrlPicture())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.workmatePicture);

        holder.workmateDesc.setText(user.getUsername());
        holder.workmateDesc.append(" is joining!");
    }

    @Override
    public int getItemCount() {
        return users.size();
    }
}
