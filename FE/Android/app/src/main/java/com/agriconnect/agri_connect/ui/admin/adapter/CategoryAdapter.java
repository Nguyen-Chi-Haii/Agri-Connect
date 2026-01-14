package com.agriconnect.agri_connect.ui.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private OnCategoryActionListener listener;

    public interface OnCategoryActionListener {
        void onEdit(Category category);
        void onDelete(Category category);
    }

    public void setOnCategoryActionListener(OnCategoryActionListener listener) {
        this.listener = listener;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvName, tvDescription;
        ImageView btnMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        void bind(Category category) {
            tvIcon.setText(category.getIcon() != null ? category.getIcon() : "📦");
            tvName.setText(category.getName());
            tvDescription.setText(category.getDescription());

            btnMore.setOnClickListener(v -> showPopupMenu(v, category));
        }

        private void showPopupMenu(View view, Category category) {
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            popup.inflate(R.menu.menu_category_action); // We need to create this menu
            
            // If menu resource doesn't exist, we can add items programmatically
            if (popup.getMenu().size() == 0) {
                popup.getMenu().add(0, 1, 0, "Chỉnh sửa");
                popup.getMenu().add(0, 2, 1, "Xóa");
            }

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.equals("Chỉnh sửa") || item.getItemId() == R.id.action_edit || item.getItemId() == 1) {
                    if (listener != null) listener.onEdit(category);
                    return true;
                } else if (title.equals("Xóa") || item.getItemId() == R.id.action_delete || item.getItemId() == 2) {
                    if (listener != null) listener.onDelete(category);
                    return true;
                }
                return false;
            });
            popup.show();
        }
    }
}
