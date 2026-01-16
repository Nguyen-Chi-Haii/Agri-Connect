package com.agriconnect.agri_connect.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatListFragment.ChatItem> chats = new ArrayList<>();
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatListFragment.ChatItem chat);
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<ChatListFragment.ChatItem> chats) {
        this.chats = chats;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(chats.get(position));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName;
        private final TextView tvLastMessage;
        private final TextView tvTime;
        private final TextView tvUnreadCount;
        private ChatListFragment.ChatItem currentItem;

        public ChatViewHolder(@NonNull View itemView, OnChatClickListener listener) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnreadCount = itemView.findViewById(R.id.tvUnreadCount);

            itemView.setOnClickListener(v -> {
                if (listener != null && currentItem != null) {
                    listener.onChatClick(currentItem);
                }
            });
        }

        public void bind(ChatListFragment.ChatItem item) {
            this.currentItem = item;
            tvUserName.setText(item.userName);
            tvLastMessage.setText(item.lastMessage);
            tvTime.setText(item.time);

            if (item.unreadCount > 0) {
                tvUnreadCount.setText(String.valueOf(item.unreadCount));
                tvUnreadCount.setVisibility(View.VISIBLE);
            } else {
                tvUnreadCount.setVisibility(View.GONE);
            }
        }
    }
}
