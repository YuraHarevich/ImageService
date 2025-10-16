package ru.kharevich.imageservice.controller.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.controller.api.ImageController;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;
import ru.kharevich.imageservice.dto.transferObjects.FileTransferEntity;
import ru.kharevich.imageservice.model.ImageType;
import ru.kharevich.imageservice.service.ImageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/images")
@RequiredArgsConstructor
public class ImageControllerImpl implements ImageController {

    private final ImageService imageService;

    @GetMapping("/id")
    @ResponseStatus(HttpStatus.OK)
    public ImageResponse getImageById(@RequestParam @Valid UUID id) {
        return imageService.getById(id);
    }

    @GetMapping("/parent")
    @ResponseStatus(HttpStatus.OK)
    public ImageResponse getImageByParent(@RequestParam @Valid UUID id) {
        return imageService.getByParentId(id);
    }

    @GetMapping("url")
    @ResponseStatus(HttpStatus.OK)
    public ImageResponse getImageByUrl(@RequestParam String url) {
        return imageService.getByUrl(url);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImageById(@Valid @RequestParam UUID id) {
        imageService.deleteById(id);
    }

    @GetMapping("/icons")
    @ResponseStatus(HttpStatus.OK)
    public List<FileTransferEntity> downloadSvgIcons() {
        return imageService.getSvgIcons();
    }

    @DeleteMapping("parent")
    @ResponseStatus(HttpStatus.OK)
    public void deleteImageByParentId(@Valid @RequestParam UUID id) {
        imageService.deleteByParentId(id);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageResponse uploadImage(
            @RequestPart("imageType") String imageType,
            @RequestPart("parentEntityId") String parentEntityId,
            @RequestPart("file") List<MultipartFile> files) {
        ImageRequest imageRequest = new ImageRequest(ImageType.POST_ATTACHMENT, UUID.fromString(parentEntityId), files);
        return imageService.save(imageRequest);
    }

}
