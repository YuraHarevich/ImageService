package ru.kharevich.imageservice.dto.response;

import ru.kharevich.imageservice.dto.transferObjects.FileTransferEntity;
import ru.kharevich.imageservice.model.ImageType;

import java.util.List;
import java.util.UUID;

public record ImageResponse(

        ImageType imageType,

        List<FileTransferEntity> files

) {
}
