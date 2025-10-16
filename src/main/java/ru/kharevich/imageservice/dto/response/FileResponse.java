package ru.kharevich.imageservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public class FileResponse {
    private String filename;
    private String url;
    private Long size;
    private String contentType;
}