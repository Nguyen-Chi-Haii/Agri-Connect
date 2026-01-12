package com.agriconnect.agri_connect.ui.admin.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.model.Post;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminPostAdapter extends RecyclerView.Adapter<AdminPostAdapter.ViewHolder> {

    private List<Post> posts = new ArrayList<>();
    private OnPostActionListener listener;

    public interface OnPostActionListener {
        void onApprove(Post post);

        void onReject(Post post);
    }

    public void setOnPostActionListener(OnPostActionListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts != null ? posts : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_post, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvSeller, tvPrice, tvStatus;
        LinearLayout layoutActions;
        MaterialButton btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvSeller = itemView.findViewById(R.id.tvSeller);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            layoutActions = itemView.findViewById(R.id.layoutActions);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }

        void bind(Post post) {
            tvTitle.setText(post.getTitle() != null ? post.getTitle() : "Không có tiêu đề");
            tvDescription.setText(post.getDescription() != null ? post.getDescription() : "");
            tvSeller.setText(post.getSellerName() != null ? post.getSellerName() : "N/A");

            // Format price
            if (post.getPrice() != null && post.getPrice() > 0) {
                NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
                tvPrice.setText(formatter.format(post.getPrice()) + "đ");
            } else {
                tvPrice.setText("Liên hệ");
            }

            // Status badge
            String status = post.getStatus();
            int bgColor;
            String statusText;

            if ("PENDING".equals(status)) {
                bgColor = R.color.warning;
                statusText = "Chờ duyệt";
                layoutActions.setVisibility(View.VISIBLE);
            } else if ("APPROVED".equals(status)) {
                bgColor = R.color.success;
                statusText = "Đã duyệt";
                layoutActions.setVisibility(View.GONE);
            } else if ("REJECTED".equals(status)) {
                bgColor = R.color.error;
                statusText = "Từ chối";
                layoutActions.setVisibility(View.GONE);
            } else {
                bgColor = R.color.text_hint;
                statusText = status != null ? status : "N/A";
                layoutActions.setVisibility(View.GONE);
            }

            tvStatus.setText(statusText);
            GradientDrawable drawable = (GradientDrawable) tvStatus.getBackground();
            if (drawable != null) {
                drawable.setColor(ContextCompat.getColor(itemView.getContext(), bgColor));
            }

            // Action buttons
            btnApprove.setOnClickListener(v -> {
                if (listener != null)
                    listener.onApprove(post);
            });

            btnReject.setOnClickListener(v -> {
                if (listener != null)
                    listener.onReject(post);
            });
        }
    }
}
