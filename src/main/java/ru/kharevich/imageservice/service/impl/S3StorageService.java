package ru.kharevich.imageservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.dto.transferObjects.FileTransferEntity;
import ru.kharevich.imageservice.exception.FileNotFoundException;
import ru.kharevich.imageservice.exception.FileUploadException;
import ru.kharevich.imageservice.exception.StaticIconUploadException;
import ru.kharevich.imageservice.service.S3StorageServiceContract;
import ru.kharevich.imageservice.util.S3Utils;
import ru.kharevich.imageservice.util.properties.S3Properties;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageService implements S3StorageServiceContract {

    private final S3Client s3Client;
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

    public List<FileTransferEntity> downloadSvgIcons() {
        String bucketName = s3Properties.getBucketName();
        List<FileTransferEntity> icons = new ArrayList<>();
        try {
            ListObjectsV2Request iconList = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix("icons/")
                    .build();

            ListObjectsV2Response listResponse = s3Client.listObjectsV2(iconList);

            if (listResponse.contents().isEmpty()) {
                throw new StaticIconUploadException("there are no icons");
            }
            for (S3Object s3Object : listResponse.contents()) {
                String s3Key = s3Object.key();
                if (S3Utils.isSvgIcon(s3Key)) {
                    try {
                        byte[] fileContent = downloadFileByKey(s3Key);
                        String fileName = S3Utils.extractFileName(s3Key);
                        icons.add(new FileTransferEntity(fileContent, fileName));
                    } catch (Exception e) {
                        log.error("Failed to download icon: {}, error: {}", s3Key, e.getMessage());
                    }
                } else {
                    log.debug("Skipping non-SVG file: {}", s3Key);
                }
            }
            return icons;
        } catch (S3Exception e) {
            log.error("S3 error while downloading SVG icons: {}", e.getMessage());
            throw new StaticIconUploadException("Failed to download SVG icons from S3");
        } catch (Exception e) {
            log.error("Unexpected error while downloading SVG icons: {}", e.getMessage());
            throw new StaticIconUploadException("Unexpected error during SVG icons download");
        }
    }

    private byte[] downloadFileByKey(String s3Key) {
        String bucketName = s3Properties.getBucketName();
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            return s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

        } catch (S3Exception e) {
            log.error("Error downloading file from S3: {}, key: {}", e.getMessage(), s3Key);
            throw new RuntimeException("Failed to download file: " + s3Key, e);
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