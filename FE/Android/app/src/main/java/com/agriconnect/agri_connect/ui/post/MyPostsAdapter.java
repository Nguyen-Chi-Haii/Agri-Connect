package com.agriconnect.agri_connect.ui.post;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.ui.home.HomeFragment;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class MyPostsAdapter extends RecyclerView.Adapter<MyPostsAdapter.MyPostViewHolder> {

    private List<HomeFragment.PostItem> posts;
    private OnPostActionListener listener;

    public interface OnPostActionListener {
        void onEditPost(HomeFragment.PostItem post);
        void onDeletePost(HomeFragment.PostItem post, int position);
    }

    public MyPostsAdapter(List<HomeFragment.PostItem> posts, OnPostActionListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MyPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_my_post, parent, false);
        return new MyPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyPostViewHolder holder, int position) {
        holder.bind(posts.get(position), position);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class MyPostViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvPrice, tvTime, tvViews;
        MaterialButton btnEdit, btnDelete;

        public MyPostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvViews = itemView.findViewById(R.id.tvViews);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        public void bind(HomeFragment.PostItem post, int position) {
            tvTitle.setText(post.content);
            tvTime.setText(post.time);
            tvViews.setText(post.viewCount + " lượt xem");

            if (post.price != null && !post.price.isEmpty()) {
                tvPrice.setText(post.price);
                tvPrice.setVisibility(View.VISIBLE);
            } else {
                tvPrice.setVisibility(View.GONE);
            }

            // Load image - simple placeholder without Glide
            if (post.imageUrl != null && !post.imageUrl.isEmpty()) {
                ivImage.setImageResource(R.drawable.ic_gallery);
            }

            // Click item to view detail
            itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(
                        itemView.getContext(), PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.id);
                itemView.getContext().startActivity(intent);
            });

            // Edit button
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditPost(post);
                }
            });

            // Delete button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeletePost(post, position);
                }
            });
        }
    }
}
