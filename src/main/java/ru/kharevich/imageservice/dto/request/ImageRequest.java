package ru.kharevich.imageservice.dto.request;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;
import ru.kharevich.imageservice.model.ImageType;

import java.util.UUID;

public record ImageRequest(

        @NotNull(message = "Image type is required")
        ImageType imageType,

        @NotNull(message = "Parent entity ID is required")
        UUID parentEntityId,

        @NotNull(message = "File is required")
        MultipartFile file,

        String name
) {

}
