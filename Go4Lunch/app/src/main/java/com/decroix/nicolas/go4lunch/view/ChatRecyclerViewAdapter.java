package com.decroix.nicolas.go4lunch.view;

import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.models.Message;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.decroix.nicolas.go4lunch.utils.UsefulFunctions.convertDateToHour;
import static com.decroix.nicolas.go4lunch.utils.UsefulFunctions.getColor;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.MessageViewHolder> {

    private final List<Message> messages;
    private final String currentUserId;

    public ChatRecyclerViewAdapter(String currentUserId) {
        this.messages = new ArrayList<>();
        this.currentUserId = currentUserId;
    }

    public void updateMessages(List<Message> messages) {
        this.messages.clear();
        this.messages.addAll(messages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_chat_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        boolean isCurrentUser = !message.getUserSender().getUid().equals(currentUserId);
        holder.layout.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        if(isCurrentUser){
            holder.layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        holder.addMessage(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.fragment_chat_item_avatar)
        ImageView avatar;
        @BindView(R.id.fragment_chat_item_text)
        TextView textViewMessage;
        @BindView(R.id.fragment_chat_item_time)
        TextView time;
        @BindView(R.id.fragment_chat_item_layout)
        ConstraintLayout layout;
        @BindView(R.id.fragment_chat_item_picture)
        ImageView picture;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        /**
         * Build the message view
         * @param message message at the position
         */
        void addMessage(Message message) {

            Glide.with(this.itemView)
                    .load(message.getUserSender().getUrlPicture())
                    .apply(RequestOptions.circleCropTransform())
                    .into(avatar);

            if (message.getUrlImage() != null && !message.getUrlImage().isEmpty()){
                picture.setVisibility(View.VISIBLE);
                Glide.with(this.itemView)
                        .load(message.getUrlImage())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(5)))
                        .into(picture);
            } else {
                picture.setVisibility(View.GONE);
            }

            if (message.getMessage() != null && !message.getMessage().isEmpty()) {
                textViewMessage.setVisibility(View.VISIBLE);
                textViewMessage.setText(message.getMessage());
                textViewMessage.getBackground()
                        .setColorFilter(getColor(message.getUserSender().getUid()), PorterDuff.Mode.MULTIPLY);
            } else {
                textViewMessage.setVisibility(View.GONE);
            }
            time.setText(convertDateToHour(message.getDateCreated()));
        }



    }


}
