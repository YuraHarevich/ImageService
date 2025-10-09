package ru.kharevich.imageservice.exception;

public class FileUploadException extends RuntimeException {
    public FileUploadException(String message) {
        super(message);
    }
}
