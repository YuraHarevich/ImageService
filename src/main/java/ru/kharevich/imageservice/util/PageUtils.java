package ru.kharevich.imageservice.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.kharevich.imageservice.dto.response.ImageResponse;

import java.util.List;

@UtilityClass
public class PageUtils {

    public static Page<ImageResponse> convertListToPage(List<ImageResponse> imageList, int page, int size) {

        int totalElements = imageList.size();
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        if (start > totalElements) {
            start = totalElements;
            end = totalElements;
        }

        List<ImageResponse> pageContent = imageList.subList(start, end);

        Pageable pageable = PageRequest.of(page, size);
        return new PageImpl<>(
                pageContent,
                pageable,
                totalElements
        );
    }

}
