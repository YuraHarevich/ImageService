package ru.kharevich.imageservice.util.mapper;


import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import ru.kharevich.imageservice.dto.request.ImageRequest;
import ru.kharevich.imageservice.dto.response.ImageResponse;
import ru.kharevich.imageservice.dto.transferObjects.FileTransferEntity;
import ru.kharevich.imageservice.model.Image;
import ru.kharevich.imageservice.model.ImageType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ImageMapper {

    @Mapping(target = "files", expression = "java(mapFileToFileTransferEntities(files,names))")
    ImageResponse toResponse(ImageType imageType, List<byte[]> files, List<String> names);

    default List<FileTransferEntity> mapFileToFileTransferEntities(List<byte[]> files, List<String> names) {
        if (files.isEmpty() || names.isEmpty() || (!Objects.equals(names.size(),files.size()))) {
            return Collections.emptyList();
        }
        List<FileTransferEntity> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            result.add(new FileTransferEntity(files.get(i), names.get(i)));
        }
        return result;
    }

    Image toEntity(ImageRequest imageRequest, String url, String name);

}