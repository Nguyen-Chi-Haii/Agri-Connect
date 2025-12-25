package vn.agriconnect.API.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.agriconnect.API.service.CloudinaryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cloudinary Service Implementation
 * Triển khai các chức năng upload, xóa và quản lý ảnh trên Cloudinary
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;
    
    @Value("${cloudinary.base-folder:agriconnect}")
    private String baseFolder;

    @Override
    public Map<String, Object> upload(MultipartFile file, String folder) {
        try {
            Map<String, Object> options = new HashMap<>();
            // Sử dụng baseFolder từ config + subfolder
            String fullFolder = baseFolder + "/" + folder;
            options.put("folder", fullFolder);
            options.put("resource_type", "auto");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);
            
            log.info("Upload thành công vào folder '{}': {}", fullFolder, result.get("secure_url"));
            return result;
        } catch (IOException e) {
            log.error("Lỗi upload file lên Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Không thể upload file: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadMultiple(List<MultipartFile> files, String folder) {
        List<String> urls = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                Map<String, Object> result = upload(file, folder);
                String url = (String) result.get("secure_url");
                urls.add(url);
            }
        }
        
        log.info("Đã upload {} files", urls.size());
        return urls;
    }

    @Override
    public boolean delete(String publicId) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            String status = (String) result.get("result");
            boolean success = "ok".equals(status);
            
            if (success) {
                log.info("Đã xóa file: {}", publicId);
            } else {
                log.warn("Không thể xóa file: {} - Status: {}", publicId, status);
            }
            
            return success;
        } catch (IOException e) {
            log.error("Lỗi xóa file trên Cloudinary: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getUrl(String publicId) {
        return cloudinary.url()
                .secure(true)
                .generate(publicId);
    }
}
