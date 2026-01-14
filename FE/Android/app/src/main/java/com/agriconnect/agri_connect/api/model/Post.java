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
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("price")
    private Double price;
    
    @SerializedName("unit")
    private String unit;
    
    @SerializedName("quantity")
    private Double quantity;
    
    @SerializedName("images")
    private List<String> images;
    
    @SerializedName("categoryId")
    private String categoryId;
    
    @SerializedName("categoryName")
    private String categoryName;
    
    @SerializedName("sellerId")
    private String sellerId;
    
    @SerializedName("sellerName")
    private String sellerName;
    
    @SerializedName("sellerAvatar")
    private String sellerAvatar;
    
    @SerializedName("sellerVerified")
    private boolean sellerVerified;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("viewCount")
    private int viewCount;
    
    @SerializedName("createdAt")
    private String createdAt;
    
    @SerializedName("location")
    private Location location;

    @SerializedName("likeCount")
    private int likeCount;

    @SerializedName("commentCount")
    private int commentCount;

    @SerializedName("liked")
    private boolean liked;

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }
    public String getUnit() { return unit; }
    public Double getQuantity() { return quantity; }
    public List<String> getImages() { return images; }
    public String getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public String getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public String getSellerAvatar() { return sellerAvatar; }
    public boolean isSellerVerified() { return sellerVerified; }
    public String getStatus() { return status; }
    public int getViewCount() { return viewCount; }
    public String getCreatedAt() { return createdAt; }
    public Location getLocation() { return location; }
    public int getLikeCount() { return likeCount; }
    public int getCommentCount() { return commentCount; }
    public boolean isLiked() { return liked; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Double price) { this.price = price; }
    public void setUnit(String unit) { this.unit = unit; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public void setImages(List<String> images) { this.images = images; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public void setSellerAvatar(String sellerAvatar) { this.sellerAvatar = sellerAvatar; }
    public void setSellerVerified(boolean sellerVerified) { this.sellerVerified = sellerVerified; }
    public void setStatus(String status) { this.status = status; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setLocation(Location location) { this.location = location; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public void setLiked(boolean liked) { this.liked = liked; }
    
    // Helper to get first image
    public String getFirstImage() {
        if (images != null && !images.isEmpty()) {
            return images.get(0);
        }
        return null;
    }
}
