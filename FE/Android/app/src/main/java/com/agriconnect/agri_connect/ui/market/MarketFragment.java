package com.agriconnect.agri_connect.ui.market;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agriconnect.agri_connect.R;
import com.agriconnect.agri_connect.api.ApiClient;
import com.agriconnect.agri_connect.api.MarketPriceApi;
import com.agriconnect.agri_connect.api.model.ApiResponse;
import com.agriconnect.agri_connect.api.model.MarketPrice;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarketFragment extends Fragment {

    private RecyclerView rvPrices;
    private ProgressBar progressBar;
    private PriceAdapter priceAdapter;
    private MarketPriceApi marketPriceApi;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_market, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize API
        if (getContext() != null) {
            marketPriceApi = ApiClient.getInstance(getContext()).getMarketPriceApi();
        }

        initViews(view);
        setupRecyclerView();
        loadPrices();
    }

    private void initViews(View view) {
        rvPrices = view.findViewById(R.id.rvPrices);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        priceAdapter = new PriceAdapter();
        rvPrices.setLayoutManager(new LinearLayoutManager(getContext()));
        rvPrices.setAdapter(priceAdapter);
    }

    private void loadPrices() {
        progressBar.setVisibility(View.VISIBLE);

        marketPriceApi.getAllPrices().enqueue(new Callback<ApiResponse<List<MarketPrice>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<MarketPrice>>> call,
                    Response<ApiResponse<List<MarketPrice>>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<MarketPrice> prices = response.body().getData();

                    if (prices != null && !prices.isEmpty()) {
                        List<PriceItem> priceItems = new ArrayList<>();
                        for (MarketPrice mp : prices) {
                            Double priceValue = mp.getAvgPrice() != null ? mp.getAvgPrice() : mp.getPrice();
                            String priceStr = priceValue != null ? String.format("%,.0f", priceValue) + "đ" : "N/A";
                            String productName = mp.getProductName() != null ? mp.getProductName()
                                    : mp.getCategoryName();

                            priceItems.add(new PriceItem(
                                    productName != null ? productName : "Sản phẩm",
                                    mp.getCategoryName() != null ? mp.getCategoryName() : "",
                                    priceStr,
                                    "/kg", // Default unit
                                    "", // change - not available in API
                                    true // positive - default
                            ));
                        }
                        priceAdapter.setData(priceItems);
                    }
                } else {
                    // Show demo data on error
                    showDemoData();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<MarketPrice>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
                showDemoData();
            }
        });
    }

    private void showDemoData() {
        // Fallback demo data when API fails
        List<PriceItem> prices = new ArrayList<>();
        prices.add(new PriceItem("Lúa ST25", "Lúa gạo", "8.500đ", "/kg", "+2.5%", true));
        prices.add(new PriceItem("Cà phê Robusta", "Cà phê", "42.000đ", "/kg", "-1.2%", false));
        prices.add(new PriceItem("Hồ tiêu đen", "Gia vị", "75.000đ", "/kg", "+0.8%", true));
        prices.add(new PriceItem("Cao su thiên nhiên", "Cao su", "38.500đ", "/kg", "+3.1%", true));
        prices.add(new PriceItem("Điều nhân", "Hạt", "210.000đ", "/kg", "-0.5%", false));
        priceAdapter.setData(prices);
    }

    public static class PriceItem {
        public String name;
        public String category;
        public String price;
        public String unit;
        public String change;
        public boolean isPositive;

        public PriceItem(String name, String category, String price, String unit, String change, boolean isPositive) {
            this.name = name;
            this.category = category;
            this.price = price;
            this.unit = unit;
            this.change = change;
            this.isPositive = isPositive;
        }
    }
}
