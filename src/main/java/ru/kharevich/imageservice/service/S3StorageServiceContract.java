package ru.kharevich.imageservice.service;

import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.dto.transferObjects.FileTransferEntity;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.List;


public interface S3StorageServiceContract {

    void createBucketIfNotExists();

    String uploadFile(MultipartFile file, String customFilename);

    byte[] downloadFile(String filename);

    String getFileUrl(String filename);

    void deleteFile(String filename);

    List<FileTransferEntity> downloadSvgIcons();

    HeadObjectResponse getFileInfo(String filename);

}
