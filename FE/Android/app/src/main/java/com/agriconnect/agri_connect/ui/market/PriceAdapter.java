package com.agriconnect.agri_connect.ui.market;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;

import java.util.ArrayList;
import java.util.List;

public class PriceAdapter extends RecyclerView.Adapter<PriceAdapter.PriceViewHolder> {

    private List<MarketFragment.PriceItem> prices = new ArrayList<>();

    public void setData(List<MarketFragment.PriceItem> prices) {
        this.prices = prices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PriceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_price, parent, false);
        return new PriceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PriceViewHolder holder, int position) {
        holder.bind(prices.get(position));
    }

    @Override
    public int getItemCount() {
        return prices.size();
    }

    static class PriceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName;
        private final TextView tvCategory;
        private final TextView tvPrice;
        private final TextView tvUnit;
        private final TextView tvChange;

        public PriceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvUnit = itemView.findViewById(R.id.tvUnit);
            tvChange = itemView.findViewById(R.id.tvChange);
        }

        public void bind(MarketFragment.PriceItem item) {
            tvProductName.setText(item.name);
            tvCategory.setText(item.category);
            tvPrice.setText(item.price);
            tvUnit.setText(item.unit);
            tvChange.setText(item.change);

            int colorRes = item.isPositive ? R.color.success : R.color.error;
            tvChange.setBackgroundTintList(
                ContextCompat.getColorStateList(itemView.getContext(), colorRes));
        }
    }
}
