package com.decroix.nicolas.go4lunch.view;

import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WorkmatesRecyclerViewAdapter extends RecyclerView.Adapter<WorkmatesRecyclerViewAdapter.ViewHolder> {

    public interface OnClickUserListener{
        void onClickUser(String restaurant);
    }

    private final List<User> users;
    private final OnClickUserListener callback;

    public List<User> getUsers() {
        return users;
    }

    public WorkmatesRecyclerViewAdapter(OnClickUserListener callback) {
        this.callback = callback;
        this.users = new ArrayList<>();
    }

    public void updateUsersList(List<User> users){
        this.users.clear();
        this.users.addAll(users);
        notifyDataSetChanged();
    }

    public void addUser(User user){
        users.add(user);
        notifyDataSetChanged();
    }

    public void clearUsers() {
        users.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_workmates_item, parent, false);
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
        if (user.getLunchRestaurantName() != null && !user.getLunchRestaurantName().isEmpty()){
            holder.workmateDesc.append(" (" + user.getLunchRestaurantName() + ")");
            holder.workmateDesc.setTextColor(Color.parseColor("#000000"));
            holder.itemView.setOnClickListener(v -> callback.onClickUser(user.getLunchRestaurantID()));
        } else {
            holder.workmateDesc.append(holder.itemView.getContext().getString(R.string.workmates_hasnt_decided_yet));
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.fragment_workmates_item_picture)
        ImageView workmatePicture;
        @BindView(R.id.fragment_workmates_item_desc)
        TextView workmateDesc;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
