package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("icon")
    private String icon;

    @SerializedName("description")
    private String description;

    @SerializedName("parentId")
    private String parentId;

    public Category() {
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
