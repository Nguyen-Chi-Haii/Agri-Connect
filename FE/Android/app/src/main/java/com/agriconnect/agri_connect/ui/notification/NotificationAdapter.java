package com.agriconnect.agri_connect.ui.notification;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }

    public NotificationAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;
        TextView tvContent;
        TextView tvTime;
        ImageView ivUnreadDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_icon);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivUnreadDot = itemView.findViewById(R.id.iv_unread_dot);
        }

        public void bind(Notification notification, OnItemClickListener listener) {
            tvTitle.setText(notification.getTitle());
            tvContent.setText(notification.getContent());
            
            // Format time if needed
            tvTime.setText(notification.getCreatedAt()); 

            if (notification.isRead()) {
                tvTitle.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                ivUnreadDot.setVisibility(View.GONE);
            } else {
                tvTitle.setTextColor(itemView.getContext().getResources().getColor(android.R.color.black));
                ivUnreadDot.setVisibility(View.VISIBLE);
            }

            // Set Icon based on type (Basic logic)
            String type = notification.getType();
            if ("NEW_MESSAGE".equals(type)) {
                ivIcon.setImageResource(R.drawable.ic_chat);
            } else if ("POST_APPROVED".equals(type) || "POST_REJECTED".equals(type)) {
                ivIcon.setImageResource(R.drawable.ic_post);
            } else {
                ivIcon.setImageResource(R.drawable.ic_notification);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(notification));
        }
    }
}
