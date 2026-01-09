package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Market price model - matches backend MarketPrice entity
 */
public class MarketPrice {
    @SerializedName("id")
    private String id;

    @SerializedName("categoryId")
    private String categoryId;

    @SerializedName("categoryName")
    private String categoryName;

    @SerializedName("date")
    private String date;

    @SerializedName("avgPrice")
    private Double avgPrice;

    @SerializedName("minPrice")
    private Double minPrice;

    @SerializedName("maxPrice")
    private Double maxPrice;

    @SerializedName("postCount")
    private int postCount;

    // Legacy field for backwards compatibility
    @SerializedName("productName")
    private String productName;

    @SerializedName("price")
    private Double price;

    @SerializedName("unit")
    private String unit;

    // Getters
    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getDate() {
        return date;
    }

    public Double getAvgPrice() {
        return avgPrice;
    }

    public Double getMinPrice() {
        return minPrice;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public int getPostCount() {
        return postCount;
    }

    public String getProductName() {
        return productName;
    }

    public Double getPrice() {
        return price != null ? price : avgPrice;
    }

    public String getUnit() {
        return unit;
    }
}
