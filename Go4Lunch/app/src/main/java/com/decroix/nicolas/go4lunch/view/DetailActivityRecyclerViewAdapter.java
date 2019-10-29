package com.decroix.nicolas.go4lunch.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.models.User;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailActivityRecyclerViewAdapter extends RecyclerView.Adapter<DetailActivityRecyclerViewAdapter.ViewHolder> {

    private final List<User> users;

    public DetailActivityRecyclerViewAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_detail_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.detail_activity_item_picture)
        ImageView workmatePicture;
        @BindView(R.id.detail_activity_item_desc)
        TextView workmateDesc;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
