package com.agriconnect.agri_connect.ui.profile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.model.StatisticsResponse;

import java.util.ArrayList;
import java.util.List;

public class CategoryStatAdapter extends RecyclerView.Adapter<CategoryStatAdapter.ViewHolder> {

    private List<StatisticsResponse.CategoryStat> stats = new ArrayList<>();
    private long maxCount = 1;

    public void setData(List<StatisticsResponse.CategoryStat> stats) {
        this.stats = stats;
        // Find max for progress calculation
        maxCount = 1;
        for (StatisticsResponse.CategoryStat stat : stats) {
            if (stat.postCount > maxCount) {
                maxCount = stat.postCount;
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_stat, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StatisticsResponse.CategoryStat stat = stats.get(position);
        holder.tvCategoryName.setText(stat.categoryName);
        holder.tvCategoryName.setText(stat.categoryName);
        holder.tvCount.setVisibility(View.GONE); // Hide count as requested
        // holder.tvCount.setText(stat.postCount + " bài");
        
        int progress = (int) ((stat.postCount * 100) / maxCount);
        holder.progressBar.setProgress(progress);
    }

    @Override
    public int getItemCount() {
        return stats.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvCount;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCount = itemView.findViewById(R.id.tvCount);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
