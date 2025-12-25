package vn.agriconnect.API.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.agriconnect.API.dto.response.ApiResponse;
import vn.agriconnect.API.service.CloudinaryService;

import java.util.List;
import java.util.Map;

/**
 * Upload Controller
 * API endpoints để upload hình ảnh lên Cloudinary
 */
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    /**
     * Upload một file
     * POST /api/upload?folder=posts
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File không được để trống"));
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Chỉ chấp nhận file ảnh"));
        }
        
        Map<String, Object> result = cloudinaryService.upload(file, folder);
        
        return ResponseEntity.ok(ApiResponse.success("Upload thành công", result));
    }

    /**
     * Upload nhiều file
     * POST /api/upload/multiple?folder=posts
     */
    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<List<String>>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", defaultValue = "general") String folder) {
        
        if (files.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Danh sách file không được để trống"));
        }
        
        // Validate all files
        for (MultipartFile file : files) {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Chỉ chấp nhận file ảnh. File không hợp lệ: " + file.getOriginalFilename()));
            }
        }
        
        List<String> urls = cloudinaryService.uploadMultiple(files, folder);
        
        return ResponseEntity.ok(ApiResponse.success("Upload " + urls.size() + " files thành công", urls));
    }

    /**
     * Xóa file theo public_id
     * DELETE /api/upload?publicId=agriconnect/posts/abc123
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteFile(@RequestParam("publicId") String publicId) {
        boolean success = cloudinaryService.delete(publicId);
        
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Xóa file thành công", null));
        } else {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Không thể xóa file"));
        }
    }
}
