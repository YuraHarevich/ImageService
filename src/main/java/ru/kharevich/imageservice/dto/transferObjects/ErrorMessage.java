package ru.kharevich.imageservice.dto.transferObjects;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
public class ErrorMessage {

    private String message;

    private LocalDateTime timestamp;

}