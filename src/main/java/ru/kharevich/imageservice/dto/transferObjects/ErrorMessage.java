package ru.kharevich.imageservice.dto.transferObjects;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
public class ErrorMessage {

    private String message;

    private LocalDateTime timestamp;

}