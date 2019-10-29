package com.decroix.nicolas.go4lunch.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.decroix.nicolas.go4lunch.R;
import com.decroix.nicolas.go4lunch.models.Message;
import com.decroix.nicolas.go4lunch.view.holders.MessageViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<MessageViewHolder> {

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

}
