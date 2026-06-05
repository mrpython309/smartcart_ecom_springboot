package com.smartcart.service;

import com.smartcart.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path uploadDir;
    private final String bucketName;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private S3Client s3Client;

    public FileStorageService(
            @Value("${app.upload.dir}") String uploadPath,
            @Value("${aws.s3.bucket:}") String bucketName,
            @Value("${aws.s3.region:us-east-1}") String region,
            @Value("${aws.s3.access-key:}") String accessKey,
            @Value("${aws.s3.secret-key:}") String secretKey) {
        
        this.uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }

        this.bucketName = bucketName;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;

        if (bucketName != null && !bucketName.trim().isEmpty()) {
            try {
                S3ClientBuilder builder = S3Client.builder()
                        .region(Region.of(region));
                
                if (accessKey != null && !accessKey.trim().isEmpty() &&
                    secretKey != null && !secretKey.trim().isEmpty()) {
                    builder.credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)
                    ));
                    log.info("S3Client configured with explicit static credentials.");
                } else {
                    log.info("No explicit AWS credentials provided. Relying on DefaultCredentialsProvider (IAM roles/profiles).");
                }
                
                this.s3Client = builder.build();
                log.info("Initialized Amazon S3 storage with bucket '{}' in region '{}'", bucketName, region);
            } catch (Exception e) {
                log.error("Failed to initialize Amazon S3 client: {}. Falling back to local storage.", e.getMessage(), e);
                this.s3Client = null;
            }
        } else {
            log.info("Amazon S3 bucket name is not configured. Falling back to local file storage.");
        }
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        java.util.List<String> allowedExtensions = java.util.Arrays.asList(".jpg", ".jpeg", ".png", ".webp", ".svg");
        if (extension.isEmpty() || !allowedExtensions.contains(extension)) {
            throw new BadRequestException("Invalid file type. Only JPG, PNG, WEBP, and SVG are allowed. Found: " + extension);
        }

        String filename = UUID.randomUUID().toString() + extension;

        if (s3Client != null) {
            try {
                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key("uploads/" + filename)
                        .contentType(contentType)
                        .build();

                s3Client.putObject(putObjectRequest, 
                        RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
                
                log.info("File uploaded successfully to S3: uploads/{}", filename);
                
                // Return public URL
                return String.format("https://%s.s3.%s.amazonaws.com/uploads/%s", bucketName, region, filename);
            } catch (Exception e) {
                log.error("Failed to upload file to S3: {}. Falling back to local storage...", e.getMessage(), e);
            }
        }

        try {
            Path targetLocation = this.uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            log.info("File stored locally: {}", filename);
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new BadRequestException("Could not store file: " + filename + ". Please try again.");
        }
    }
}
