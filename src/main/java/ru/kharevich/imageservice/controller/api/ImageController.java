package ru.kharevich.imageservice.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;
import ru.kharevich.imageservice.dto.transferObjects.FileTransferEntity;
import ru.kharevich.imageservice.model.ImageType;

import java.util.List;
import java.util.UUID;

public interface ImageController {

    ImageResponse getImageById(@Valid UUID id);

    public ImageResponse getImageByUrl(String url);

    void deleteImageById(@Valid UUID id);

    List<FileTransferEntity> downloadSvgIcons();

    ImageResponse uploadImage(
            @RequestPart("imageType") String imageType,
            @RequestPart("parentEntityId") String parentEntityId,
            @RequestPart("file") List<MultipartFile> file);
}
