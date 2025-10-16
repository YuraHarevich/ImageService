package ru.kharevich.imageservice.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.dto.response.ImageResponse;
import ru.kharevich.imageservice.dto.response.PageableResponse;
import ru.kharevich.imageservice.dto.transferObjects.FileTransferEntity;

import java.util.List;
import java.util.UUID;

public interface ImageController {

    ImageResponse getImageById(@Valid UUID id);

    PageableResponse<ImageResponse> getImagesByParent(@RequestBody List<UUID> ids,
                                                      @RequestParam(defaultValue = "0") @Min(0) int page_number,
                                                      @RequestParam(defaultValue = "10") int size);

    ImageResponse getImageByUrl(String url);

    void deleteImageById(@Valid UUID id);

    List<FileTransferEntity> downloadSvgIcons();

    ImageResponse uploadImage(
            @RequestPart("imageType") String imageType,
            @RequestPart("parentEntityId") String parentEntityId,
            @RequestPart("file") List<MultipartFile> file);
}
