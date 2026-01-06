package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Generic Paged Response wrapper
 */
public class PagedResponse<T> {
    @SerializedName("content")
    private List<T> content;
    
    @SerializedName("currentPage")
    private int currentPage;
    
    @SerializedName("size")
    private int size;
    
    @SerializedName("totalElements")
    private long totalElements;
    
    @SerializedName("totalPages")
    private int totalPages;
    
    @SerializedName("last")
    private boolean last;

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isLast() { return last; }
    public void setLast(boolean last) { this.last = last; }
}
