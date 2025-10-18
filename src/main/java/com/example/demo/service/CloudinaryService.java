package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload image to Cloudinary
     * @param file The image file to upload
     * @return The secure URL of the uploaded image
     */
    public String uploadImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "ute-fashion/reviews/images",
                        "resource_type", "image",
                        "quality", "auto:good",
                        "fetch_format", "auto"
                ));
        
        log.info("Image uploaded successfully: {}", uploadResult.get("secure_url"));
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Upload video to Cloudinary
     * @param file The video file to upload
     * @return The secure URL of the uploaded video
     */
    public String uploadVideo(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "ute-fashion/reviews/videos",
                        "resource_type", "video",
                        "quality", "auto:good"
                ));
        
        log.info("Video uploaded successfully: {}", uploadResult.get("secure_url"));
        return (String) uploadResult.get("secure_url");
    }

    /**
     * Delete a resource from Cloudinary by public ID
     * @param publicId The public ID of the resource
     * @param resourceType The type of resource (image, video, raw)
     */
    public void deleteFile(String publicId, String resourceType) throws IOException {
        Map result = cloudinary.uploader().destroy(publicId,
                ObjectUtils.asMap("resource_type", resourceType));
        log.info("File deleted: {} - Result: {}", publicId, result.get("result"));
    }

    /**
     * Extract public ID from Cloudinary URL
     * @param url The Cloudinary URL
     * @return The public ID
     */
    public String extractPublicId(String url) {
        // Example URL: https://res.cloudinary.com/Fashion/image/upload/v1234567890/ute-fashion/reviews/images/abc123.jpg
        // Public ID: ute-fashion/reviews/images/abc123
        String[] parts = url.split("/upload/");
        if (parts.length > 1) {
            String pathWithVersion = parts[1];
            // Remove version number (v1234567890/)
            String path = pathWithVersion.replaceFirst("v\\d+/", "");
            // Remove file extension
            int lastDot = path.lastIndexOf('.');
            return lastDot > 0 ? path.substring(0, lastDot) : path;
        }
        return null;
    }
}
