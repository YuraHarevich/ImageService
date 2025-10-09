package ru.kharevich.imageservice.controller.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;

import java.util.UUID;

public interface ImageController {

    ImageResponse getImageById(@Valid UUID id);

    public ImageResponse getImageByUrl(String url);

    void deleteImageById(@Valid UUID id);

    ImageResponse uploadImage(@Valid ImageRequest imageRequest);

}
