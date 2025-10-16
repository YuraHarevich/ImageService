package ru.kharevich.imageservice.service;

import jakarta.validation.Valid;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;
import ru.kharevich.imageservice.dto.response.PageableResponse;
import ru.kharevich.imageservice.dto.transferObjects.FileTransferEntity;

import java.util.List;
import java.util.UUID;

public interface ImageService {

    ImageResponse getById(UUID id);

    ImageResponse getByUrl(String url);

    void deleteById(UUID id);

    ImageResponse save(ImageRequest imageRequest);

    ImageResponse getByParentId(@Valid UUID id);

    void deleteByParentId(@Valid UUID id);

    List<FileTransferEntity> getSvgIcons();

    PageableResponse<ImageResponse> getManyByParentId(List<UUID> ids, int page_number, int size);
}
