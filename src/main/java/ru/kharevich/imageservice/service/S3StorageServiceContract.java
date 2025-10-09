package ru.kharevich.imageservice.service;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;


public interface S3StorageServiceContract {

    void createBucketIfNotExists();

    String uploadFile(MultipartFile file, String customFilename);

    byte[] downloadFile(String filename);

    String getFileUrl(String filename);

    void deleteFile(String filename);

    HeadObjectResponse getFileInfo(String filename);

}
