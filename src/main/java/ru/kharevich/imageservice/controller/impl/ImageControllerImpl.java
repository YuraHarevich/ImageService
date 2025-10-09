package ru.kharevich.imageservice.controller.impl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kharevich.imageservice.controller.api.ImageController;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;
import ru.kharevich.imageservice.model.Image;
import ru.kharevich.imageservice.service.ImageService;

import java.util.UUID;

@RestController
@RequestMapping("api/v1/images")
@RequiredArgsConstructor
public class ImageControllerImpl implements ImageController {

    private final ImageService imageService;

    @GetMapping("/id")
    public ImageResponse getImageById(@RequestParam @Valid UUID id) {
        return imageService.getById(id);
    }

    @GetMapping("url")
    public ImageResponse getImageByUrl(@RequestParam String url) {
        return imageService.getByUrl(url);
    }

    @DeleteMapping
    public void deleteImageById(@Valid @RequestParam UUID id) {
        imageService.deleteById(id);
    }

    @PostMapping
    public ImageResponse uploadImage(@Valid ImageRequest imageRequest) {
        return imageService.save(imageRequest);
    }

}
