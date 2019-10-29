package com.decroix.nicolas.go4lunch.view.holders;

import android.graphics.PorterDuff;
import android.view.View;
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

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.decroix.nicolas.go4lunch.utils.UsefulFunctions.convertDateToHour;
import static com.decroix.nicolas.go4lunch.utils.UsefulFunctions.getColor;

public class MessageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.fragment_chat_item_avatar)
    public ImageView avatar;
    @BindView(R.id.fragment_chat_item_text)
    public TextView textViewMessage;
    @BindView(R.id.fragment_chat_item_time)
    public TextView time;
    @BindView(R.id.fragment_chat_item_layout)
    public ConstraintLayout layout;
    @BindView(R.id.fragment_chat_item_picture)
    public ImageView picture;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    /**
     * Build the message view
     * @param message message at the position
     */
    public void addMessage(Message message) {

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