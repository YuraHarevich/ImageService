package ru.kharevich.imageservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;
import ru.kharevich.imageservice.exception.FileUploadException;
import ru.kharevich.imageservice.exception.ImageNotFoundException;
import ru.kharevich.imageservice.model.Image;
import ru.kharevich.imageservice.repository.ImageRepository;
import ru.kharevich.imageservice.service.ImageService;
import ru.kharevich.imageservice.util.mapper.ImageMapper;
import ru.kharevich.imageservice.util.validation.ImageValidationService;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    private final ImageMapper imageMapper;

    private final S3StorageService s3StorageService;

    private final ImageValidationService imageValidationService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImageResponse getById(UUID id) {
        Image image =  imageValidationService.findByIdThrowsExceptionIfDoesntExist(id, new ImageNotFoundException("Image with id {} not found".formatted(id)));
        byte[] file = s3StorageService.downloadFile(image.getName());
        return imageMapper.toResponse(image, file);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImageResponse getByUrl(String url) {
        Image image =  imageValidationService.findByUrlThrowsExceptionIfDoesntExist(url, new ImageNotFoundException("Image with url {} not found".formatted(url)));
        byte[] file = s3StorageService.downloadFile(image.getName());
        return imageMapper.toResponse(image, file);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteById(UUID id) {
        Image image =  imageValidationService.findByIdThrowsExceptionIfDoesntExist(id, new ImageNotFoundException("Image with id {} not found".formatted(id)));
        imageRepository.deleteById(id);
        s3StorageService.deleteFile(image.getName());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImageResponse save(ImageRequest imageRequest) {
        if (imageRequest.name() == null) {
            String name = generateFilename(imageRequest.file());
            imageRequest = new ImageRequest(imageRequest.imageType(),
                    imageRequest.parentEntityId(),
                    imageRequest.file(),
                    name);
        }
        String url = s3StorageService.uploadFile(imageRequest.file(), imageRequest.name());
        Image image = imageMapper.toEntity(imageRequest, url);
        imageRepository.saveAndFlush(image);
        ImageResponse response;
        try {
           response = imageMapper.toResponse(image, imageRequest.file().getBytes());
        } catch (IOException e) {
            throw new FileUploadException("Unable to upload file");
        }
        return response;
    }

    private String generateFilename(MultipartFile file) {
        String extension = getFileExtension(file);
        long timestamp = System.currentTimeMillis();
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return timestamp + random + extension;
    }

    private String getFileExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return "";
    }

}
