package com.agriconnect.agri_connect.ui.home;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.ui.post.PostDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<HomeFragment.PostItem> posts = new ArrayList<>();

    public void setData(List<HomeFragment.PostItem> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName;
        private final TextView tvPostTime;
        private final TextView tvContent;
        private final TextView tvPrice;
        private final TextView tvLikeCount;
        private final TextView tvCommentCount;
        private final TextView tvViews;
        private final ImageView ivVerified;
        private final ImageView ivPostImage;
        private final View btnLike;
        private final View btnComment;
        private final ImageView ivLike;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvPostTime = itemView.findViewById(R.id.tvPostTime);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            tvViews = itemView.findViewById(R.id.tvViews);
            ivVerified = itemView.findViewById(R.id.ivVerified);
            ivPostImage = itemView.findViewById(R.id.ivPostImage);
            btnLike = itemView.findViewById(R.id.btnLike);
            btnComment = itemView.findViewById(R.id.btnComment);
            ivLike = itemView.findViewById(R.id.ivLike);
        }

        public void bind(HomeFragment.PostItem post) {
            tvUserName.setText(post.userName);
            tvPostTime.setText(post.time);
            tvContent.setText(post.content);
            tvLikeCount.setText(String.valueOf(post.likeCount));
            tvCommentCount.setText(String.valueOf(post.commentCount));
            tvViews.setText(post.viewCount + " lượt xem");
            
            ivVerified.setVisibility(post.isVerified ? View.VISIBLE : View.GONE);
            
            if (post.price != null && !post.price.isEmpty()) {
                tvPrice.setText(post.price);
                tvPrice.setVisibility(View.VISIBLE);
            } else {
                tvPrice.setVisibility(View.GONE);
            }

            // Load post image - simple approach without Glide
            if (post.imageUrl != null && !post.imageUrl.isEmpty()) {
                ivPostImage.setVisibility(View.VISIBLE);
                // For now, just show placeholder - real implementation needs async loading
                ivPostImage.setImageResource(R.drawable.ic_gallery);
            } else {
                ivPostImage.setVisibility(View.GONE);
            }

            // Click to view detail
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.id);
                itemView.getContext().startActivity(intent);
            });

            // Like button click
            btnLike.setOnClickListener(v -> {
                // Toggle like locally
                post.likeCount++;
                tvLikeCount.setText(String.valueOf(post.likeCount));
                // TODO: Call API
            });

            // Comment button click
            btnComment.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.id);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
