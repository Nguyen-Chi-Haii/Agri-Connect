package com.agriconnect.agri_connect.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;

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
        }
    }
}
