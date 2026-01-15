package com.agriconnect.agri_connect.ui.home;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.model.Category;

import java.util.ArrayList;
import java.util.List;

public class HomeCategoryAdapter extends RecyclerView.Adapter<HomeCategoryAdapter.ViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private String selectedCategoryId = null; // null means "All"
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category); // category null means "All"
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }
    
    public void setSelectedCategoryId(String id) {
        this.selectedCategoryId = id;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Position 0 is "All", others are categories given + 1 offset logic? 
        // Or better: just manage a list that includes a fake "All" category or handle it separately.
        // Let's handle it by adding a fake "All" option to the list passed in, OR managing it inside.
        // Managing inside is cleaner for the caller.
        
        if (position == 0) {
            // "All" Item
            holder.tvIcon.setText("🏠");
            holder.tvName.setText("Tất cả");
            
            boolean isSelected = selectedCategoryId == null;
            updateSelection(holder, isSelected);
            
            holder.itemView.setOnClickListener(v -> {
                if (selectedCategoryId != null) {
                    selectedCategoryId = null;
                    notifyDataSetChanged();
                    if (listener != null) listener.onCategoryClick(null);
                }
            });
        } else {
            Category category = categories.get(position - 1);
            holder.tvIcon.setText(category.getIcon() != null ? category.getIcon() : "📦");
            holder.tvName.setText(category.getName());
            
            boolean isSelected = category.getId().equals(selectedCategoryId);
            updateSelection(holder, isSelected);
            
            holder.itemView.setOnClickListener(v -> {
                if (!category.getId().equals(selectedCategoryId)) {
                    selectedCategoryId = category.getId();
                    notifyDataSetChanged();
                    if (listener != null) listener.onCategoryClick(category);
                }
            });
        }
    }

    private void updateSelection(ViewHolder holder, boolean isSelected) {
        if (isSelected) {
            holder.layoutContent.setBackgroundResource(R.color.primary); // Should use a drawable ideally, but color works for filling view
            holder.tvName.setTextColor(Color.WHITE);
            holder.tvIcon.setAlpha(1.0f);
        } else {
            holder.layoutContent.setBackgroundColor(Color.TRANSPARENT);
            holder.tvName.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.text_primary));
            holder.tvIcon.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size() + 1; // +1 for "All"
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName;
        LinearLayout layoutContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvName = itemView.findViewById(R.id.tvName);
            layoutContent = itemView.findViewById(R.id.layoutContent);
        }
    }
}
