package ru.kharevich.imageservice.service;

import jakarta.validation.Valid;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;

import java.util.UUID;

public interface ImageService {

    ImageResponse getById(UUID id);

    ImageResponse getByUrl(String url);

    void deleteById(UUID id);

    ImageResponse save(ImageRequest imageRequest);

    ImageResponse getByParentId(@Valid UUID id);

    void deleteByParentId(@Valid UUID id);
}
