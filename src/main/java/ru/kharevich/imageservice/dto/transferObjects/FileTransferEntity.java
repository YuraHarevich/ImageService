package ru.kharevich.imageservice.dto.transferObjects;

public record FileTransferEntity(
        byte[] file,
        String name
) {
}
