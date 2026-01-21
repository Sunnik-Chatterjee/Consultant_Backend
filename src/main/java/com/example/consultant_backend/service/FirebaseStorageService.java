package com.example.consultant_backend.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class FirebaseStorageService {

    @Value("${firebase.storage.bucket}")
    private String bucketName;

    public String uploadPrescriptionImage(MultipartFile file, Long appointmentId, String type) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Generate unique filename
        String fileName = "prescriptions/" + appointmentId + "/" + type + "_" + UUID.randomUUID() + getFileExtension(file.getOriginalFilename());

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        Storage storage = StorageClient.getInstance().bucket().getStorage();
        storage.create(blobInfo, file.getBytes());

        // Generate permanent public URL (never expires)
        String permanentUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucketName,
                fileName.replace("/", "%2F"));

        log.info("Uploaded {} for appointment {}: {}", type, appointmentId, fileName);
        return permanentUrl;
    }

    public String uploadImage(MultipartFile file, String folderPath) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String fileName = folderPath + "/" + UUID.randomUUID() + getFileExtension(file.getOriginalFilename());

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        Storage storage = StorageClient.getInstance().bucket().getStorage();
        storage.create(blobInfo, file.getBytes());

        // Generate permanent public URL (never expires)
        String permanentUrl = String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                bucketName,
                fileName.replace("/", "%2F"));

        log.info("Uploaded image: {}", fileName);
        return permanentUrl;
    }

    public boolean deleteImage(String imageUrl) {
        try {
            String fileName = extractFileNameFromUrl(imageUrl);
            BlobId blobId = BlobId.of(bucketName, fileName);
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            boolean deleted = storage.delete(blobId);

            log.info("Deleted image: {}, Success: {}", fileName, deleted);
            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete image: {}", imageUrl, e);
            return false;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    private String extractFileNameFromUrl(String url) {
        // Extract filename from Firebase Storage URL
        if (url.contains("firebasestorage.googleapis.com")) {
            // Extract from URL like: https://firebasestorage.googleapis.com/v0/b/bucket/o/path%2Ffile.jpg?alt=media
            String encoded = url.substring(url.indexOf("/o/") + 3, url.indexOf("?alt=media"));
            return encoded.replace("%2F", "/");
        } else if (url.contains("storage.googleapis.com")) {
            return url.substring(url.indexOf(bucketName) + bucketName.length() + 1);
        }
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
