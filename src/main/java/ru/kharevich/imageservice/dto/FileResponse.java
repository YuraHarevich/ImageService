package ru.kharevich.imageservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileResponse {
    private String filename;
    private String url;
    private Long size;
    private String contentType;
}