package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Market price model
 */
public class MarketPrice {
    @SerializedName("id")
    private String id;
    
    @SerializedName("productName")
    private String productName;
    
    @SerializedName("categoryId")
    private String categoryId;
    
    @SerializedName("categoryName")
    private String categoryName;
    
    @SerializedName("price")
    private Double price;
    
    @SerializedName("unit")
    private String unit;
    
    @SerializedName("location")
    private String location;
    
    @SerializedName("recordedAt")
    private String recordedAt;

    // Getters
    public String getId() { return id; }
    public String getProductName() { return productName; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Double getPrice() { return price; }
    public String getUnit() { return unit; }
    public String getLocation() { return location; }
    public String getRecordedAt() { return recordedAt; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setPrice(Double price) { this.price = price; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setLocation(String location) { this.location = location; }
    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }
}
