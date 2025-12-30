package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Post detail response
 */
public class Post {
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("content")
    private String content;
    
    @SerializedName("price")
    private Double price;
    
    @SerializedName("unit")
    private String unit;
    
    @SerializedName("quantity")
    private Double quantity;
    
    @SerializedName("imageUrls")
    private List<String> imageUrls;
    
    @SerializedName("categoryId")
    private String categoryId;
    
    @SerializedName("categoryName")
    private String categoryName;
    
    @SerializedName("sellerId")
    private String sellerId;
    
    @SerializedName("sellerName")
    private String sellerName;
    
    @SerializedName("sellerAvatarUrl")
    private String sellerAvatarUrl;
    
    @SerializedName("sellerVerified")
    private boolean sellerVerified;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("viewCount")
    private int viewCount;
    
    @SerializedName("createdAt")
    private String createdAt;

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Double getPrice() { return price; }
    public String getUnit() { return unit; }
    public Double getQuantity() { return quantity; }
    public List<String> getImageUrls() { return imageUrls; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getSellerAvatarUrl() { return sellerAvatarUrl; }
    public boolean isSellerVerified() { return sellerVerified; }
    public String getStatus() { return status; }
    public int getViewCount() { return viewCount; }
    public String getCreatedAt() { return createdAt; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setPrice(Double price) { this.price = price; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public void setSellerAvatarUrl(String sellerAvatarUrl) { this.sellerAvatarUrl = sellerAvatarUrl; }
    public void setSellerVerified(boolean sellerVerified) { this.sellerVerified = sellerVerified; }
    public void setStatus(String status) { this.status = status; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
