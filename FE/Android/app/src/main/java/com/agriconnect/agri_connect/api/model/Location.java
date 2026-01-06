package com.agriconnect.agri_connect.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Location model
 */
public class Location {
    @SerializedName("province")
    private String province;
    
    @SerializedName("district")
    private String district;
    
    @SerializedName("ward")
    private String ward;
    
    @SerializedName("detail")
    private String detail;

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getWard() { return ward; }
    public void setWard(String ward) { this.ward = ward; }

    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (detail != null && !detail.isEmpty()) sb.append(detail).append(", ");
        if (ward != null && !ward.isEmpty()) sb.append(ward).append(", ");
        if (district != null && !district.isEmpty()) sb.append(district).append(", ");
        if (province != null && !province.isEmpty()) sb.append(province);
        
        String result = sb.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }
        return result;
    }
}
