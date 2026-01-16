package com.agriconnect.agri_connect.ui.home;

import android.content.Intent;
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

    private OnPostClickListener listener;
    private OnLikeClickListener likeListener;

    public interface OnPostClickListener {
        void onPostClick(String postId);
    }

    public interface OnLikeClickListener {
        void onLikeClick(String postId, int position);
    }

    public void setOnPostClickListener(OnPostClickListener listener) {
        this.listener = listener;
    }

    public void setOnLikeClickListener(OnLikeClickListener listener) {
        this.likeListener = listener;
    }

    // Update like count for a specific position
    public void updateLikeCount(int position, int newLikeCount, boolean isLiked) {
        if (position >= 0 && position < posts.size()) {
            posts.get(position).likeCount = newLikeCount;
            posts.get(position).isLiked = isLiked;
            notifyItemChanged(position);
        }
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
        holder.bind(posts.get(position), listener, likeListener, position);
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

        public void bind(HomeFragment.PostItem post, OnPostClickListener listener, OnLikeClickListener likeListener,
                int position) {
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

            // Update like icon based on liked state
            if (ivLike != null) {
                ivLike.setImageResource(post.isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart);
                ivLike.setColorFilter(post.isLiked ? itemView.getContext().getResources().getColor(R.color.error)
                        : itemView.getContext().getResources().getColor(R.color.text_secondary));
            }

            // Load post image
            if (post.imageUrl != null && !post.imageUrl.isEmpty()) {
                ivPostImage.setVisibility(View.VISIBLE);
                ivPostImage.setImageResource(R.drawable.ic_gallery);
            } else {
                ivPostImage.setVisibility(View.GONE);
            }

            // Click to view detail
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onPostClick(post.id);
                } else {
                    Intent intent = new Intent(itemView.getContext(), PostDetailActivity.class);
                    intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.id);
                    itemView.getContext().startActivity(intent);
                }
            });

            // Like button click - call API
            btnLike.setOnClickListener(v -> {
                if (likeListener != null) {
                    likeListener.onLikeClick(post.id, position);
                }
            });

            // Comment button click - go to detail
            btnComment.setOnClickListener(v -> {
                Intent intent = new Intent(itemView.getContext(), PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.id);
                intent.putExtra("showComments", true);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
