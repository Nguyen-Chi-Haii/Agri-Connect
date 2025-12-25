package vn.agriconnect.API.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import vn.agriconnect.API.exception.BadRequestException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Component
public class FileUploadUtils {

    private static final String UPLOAD_DIR = "uploads";

    public String uploadFile(MultipartFile file, String subDirectory) {
        validateFile(file);
        
        try {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(UPLOAD_DIR, subDirectory);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            return filePath.toString();
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log error but don't throw
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        
        if (file.getSize() > Constants.MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !Arrays.asList(Constants.ALLOWED_IMAGE_TYPES).contains(contentType)) {
            throw new BadRequestException("Invalid file type");
        }
    }
}
