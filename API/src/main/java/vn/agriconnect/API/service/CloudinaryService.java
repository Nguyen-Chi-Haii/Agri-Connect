package vn.agriconnect.API.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Cloudinary Service Interface
 * Service để upload và quản lý hình ảnh trên Cloudinary
 */
public interface CloudinaryService {
    
    /**
     * Upload một file lên Cloudinary
     * @param file File cần upload
     * @param folder Folder trên Cloudinary (vd: "posts", "avatars", "products")
     * @return Map chứa thông tin upload (url, public_id, etc.)
     */
    Map<String, Object> upload(MultipartFile file, String folder);
    
    /**
     * Upload nhiều file lên Cloudinary
     * @param files Danh sách file cần upload
     * @param folder Folder trên Cloudinary
     * @return Danh sách URL của các file đã upload
     */
    List<String> uploadMultiple(List<MultipartFile> files, String folder);
    
    /**
     * Xóa file trên Cloudinary theo public_id
     * @param publicId Public ID của file cần xóa
     * @return true nếu xóa thành công
     */
    boolean delete(String publicId);
    
    /**
     * Lấy URL của ảnh đã upload
     * @param publicId Public ID của ảnh
     * @return URL đầy đủ của ảnh
     */
    String getUrl(String publicId);
}
