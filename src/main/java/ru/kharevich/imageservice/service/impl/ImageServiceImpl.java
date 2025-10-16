package ru.kharevich.imageservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;
import ru.kharevich.imageservice.dto.response.PageableResponse;
import ru.kharevich.imageservice.dto.transferObjects.FileTransferEntity;
import ru.kharevich.imageservice.exception.FileUploadException;
import ru.kharevich.imageservice.exception.ImageNotFoundException;
import ru.kharevich.imageservice.model.Image;
import ru.kharevich.imageservice.repository.ImageRepository;
import ru.kharevich.imageservice.service.ImageService;
import ru.kharevich.imageservice.util.PageUtils;
import ru.kharevich.imageservice.util.mapper.ImageMapper;
import ru.kharevich.imageservice.util.mapper.PageMapper;
import ru.kharevich.imageservice.util.validation.ImageValidationService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;

    private final ImageMapper imageMapper;

    private final S3StorageService s3StorageService;

    private final ImageValidationService imageValidationService;

    private final PageMapper pageMapper;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImageResponse getById(UUID id) {
        Image image = imageValidationService.findByIdThrowsExceptionIfDoesntExist(id, new ImageNotFoundException("Image with id {} not found".formatted(id)));
        byte[] file = s3StorageService.downloadFile(image.getName());
        return imageMapper.toResponse(
                image.getImageType(),
                Collections.singletonList(file),
                Collections.singletonList(image.getName()),
                image.getParentEntityId());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImageResponse getByUrl(String url) {
        Image image = imageValidationService.findByUrlThrowsExceptionIfDoesntExist(url, new ImageNotFoundException("Image with url {} not found".formatted(url)));
        byte[] file = s3StorageService.downloadFile(image.getName());
        return imageMapper.toResponse(
                image.getImageType(),
                Collections.singletonList(file),
                Collections.singletonList(image.getName()),
                image.getParentEntityId()
        );
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void deleteById(UUID id) {
        Image image = imageValidationService.findByIdThrowsExceptionIfDoesntExist(
                id,
                new ImageNotFoundException("Image with id not found")
        );
        imageRepository.deleteById(id);
        s3StorageService.deleteFile(image.getName());
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ImageResponse save(ImageRequest imageRequest) {
        ImageResponse response;
        List<String> names = new ArrayList<>();
        imageRequest.files().forEach(image -> {
            String name = generateFilename(image, imageRequest.parentEntityId());
            names.add(name);
            String url = s3StorageService.uploadFile(image, name);
            Image imageToSave = imageMapper.toEntity(imageRequest, url, name);
            imageRepository.saveAndFlush(imageToSave);
        });
        response = imageMapper.toResponse(
                imageRequest.imageType(),
                convertFromMultifile(imageRequest.files()),
                names,
                imageRequest.parentEntityId()
        );

        return response;
    }

    @Override
    public ImageResponse getByParentId(UUID parentId) {
        List<Image> images = imageRepository.findByParentEntityId(parentId);
        if (images.isEmpty())
            throw new ImageNotFoundException("no image");
        List<byte[]> bytes = new ArrayList<>();
        List<String> names = new ArrayList<>();
        images.forEach(image -> {
            bytes.add(s3StorageService.downloadFile(image.getName()));
            names.add(image.getName());
        });
        return imageMapper.toResponse(
                images.getFirst().getImageType(),
                bytes,
                names,
                parentId
        );
    }

    public PageableResponse<ImageResponse> getManyByParentId(List<UUID> ids, int page_number, int size) {
        List<ImageResponse> responses = ids.
                stream()
                .map(this::getByParentId)
                .toList();
        Page<ImageResponse> responsePage = PageUtils.convertListToPage(responses, page_number, size);
        return pageMapper.toResponse(responsePage);
    }

    public void deleteByParentId(UUID parentId) {
        List<Image> images = imageRepository.findByParentEntityId(parentId);
        images.stream().forEach(image -> {
            imageRepository.deleteById(image.getId());
            s3StorageService.deleteFile(image.getName());
        });
    }

    public List<FileTransferEntity> getSvgIcons() {
        return s3StorageService.downloadSvgIcons();
    }

    private String generateFilename(MultipartFile file, UUID parentId) {
        return file.getOriginalFilename() + "-" + parentId.toString();
    }

    private List<byte[]> convertFromMultifile(List<MultipartFile> files) {
        List<byte[]> bytes = files.stream().map((file) -> {
            try {
                return file.getBytes();
            } catch (IOException e) {
                throw new FileUploadException("unable convert file");
            }
        }).toList();
        return bytes;
    }

}
