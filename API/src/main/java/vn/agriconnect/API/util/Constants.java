package vn.agriconnect.API.util;

public final class Constants {
    
    private Constants() {}
    
    // JWT
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final long ACCESS_TOKEN_EXPIRATION = 86400000; // 1 day
    public static final long REFRESH_TOKEN_EXPIRATION = 604800000; // 7 days
    
    // Pagination
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    
    // File Upload
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    public static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/webp"};
    
    // Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_FARMER = "ROLE_FARMER";
    public static final String ROLE_BUYER = "ROLE_BUYER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
}
