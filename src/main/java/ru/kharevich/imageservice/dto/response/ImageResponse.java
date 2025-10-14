package ru.kharevich.imageservice.dto.response;

import ru.kharevich.imageservice.model.ImageType;

import java.util.UUID;

public record ImageResponse(

        UUID id,

        String url,

        ImageType imageType,

        String name,

        byte[] file

) {
}
