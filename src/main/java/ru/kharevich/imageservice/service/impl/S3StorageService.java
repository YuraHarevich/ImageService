package ru.kharevich.imageservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.exception.FileNotFoundException;
import ru.kharevich.imageservice.exception.FileUploadException;
import ru.kharevich.imageservice.service.S3StorageServiceContract;
import ru.kharevich.imageservice.util.properties.S3Properties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements S3StorageServiceContract {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;


    public void createBucketIfNotExists() {
        String bucketName = s3Properties.getBucketName();

        ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
        List<Bucket> buckets = s3Client.listBuckets(listBucketsRequest).buckets();

        boolean bucketExists = buckets.stream()
                .anyMatch(bucket -> bucket.name().equals(bucketName));

        if (!bucketExists) {
            CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                    .bucket(bucketName)
                    .build();
            s3Client.createBucket(createBucketRequest);
            log.info("Bucket created: {}", bucketName);
        } else {
            log.info("Bucket already exists: {}", bucketName);
        }
    }

    public String uploadFile(MultipartFile file, String customFilename) {
        String bucketName = s3Properties.getBucketName();
        try {
            String filename = customFilename != null ? customFilename :
                    UUID.randomUUID() + "_" + file.getOriginalFilename();

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromBytes(file.getBytes()));
            log.info("File uploaded successfully: {}", filename);
            return getFileUrl(filename);

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new FileUploadException("Failed to upload file");
        }
    }

    public byte[] downloadFile(String filename) {
        String bucketName = s3Properties.getBucketName();
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

        } catch (S3Exception e) {
            log.error("Error downloading file: {}", e.getMessage());
            throw new FileNotFoundException("File not found: " + filename);
        }
    }

    public String getFileUrl(String filename) {
        String bucketName = s3Properties.getBucketName();
        return String.format("%s/%s/%s",
                s3Properties.getEndpoint(),
                bucketName,
                filename);
    }

    public void deleteFile(String filename) {
        String bucketName = s3Properties.getBucketName();
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("File deleted: {}", filename);

        } catch (S3Exception e) {
            log.error("Error deleting file: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file: " + filename, e);
        }
    }

    public HeadObjectResponse getFileInfo(String filename) {
        String bucketName = s3Properties.getBucketName();
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            return s3Client.headObject(headObjectRequest);
        } catch (S3Exception e) {
            log.error("Error getting file info: {}", e.getMessage());
            throw new RuntimeException("File not found: " + filename, e);
        }
    }

}