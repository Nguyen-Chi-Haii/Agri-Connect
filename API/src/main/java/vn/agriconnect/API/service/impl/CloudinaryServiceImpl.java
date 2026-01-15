package vn.agriconnect.API.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.agriconnect.API.service.CloudinaryService;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.base-folder:agriconnect}")
    private String baseFolder;

    @Override
    public Map<String, Object> upload(MultipartFile file, String folder) {
        try {
            String fileName = UUID.randomUUID().toString();
            Map params = ObjectUtils.asMap(
                    "public_id", (folder != null ? folder + "/" : baseFolder + "/") + fileName,
                    "folder", (folder != null ? folder : baseFolder)
            );
            return cloudinary.uploader().upload(file.getBytes(), params);
        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    @Override
    public java.util.List<String> uploadMultiple(java.util.List<MultipartFile> files, String folder) {
        return files.stream().map(file -> {
            Map<String, Object> result = upload(file, folder);
            return (String) result.get("secure_url");
        }).toList();
    }

    @Override
    public boolean delete(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public String getUrl(String publicId) {
        return cloudinary.url().generate(publicId);
    }
}
