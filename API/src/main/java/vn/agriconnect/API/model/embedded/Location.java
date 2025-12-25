package vn.agriconnect.API.model.embedded;

import lombok.Data;

/**
 * Location Information - Embedded Document
 */
@Data
public class Location {
    private String province; // Tỉnh
    private String district; // Huyện
    private String ward;     // Xã
    private String detail;   // Địa chỉ chi tiết
}
