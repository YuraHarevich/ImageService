package ru.kharevich.imageservice.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.kharevich.imageservice.service.impl.S3StorageService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BucketInitializer {

    private final S3StorageService s3StorageService;

    @PostConstruct
    public void init() {
        try {
            s3StorageService.createBucketIfNotExists();
            log.info("S3 bucket initialization completed");
        } catch (Exception e) {
            log.error("Failed to initialize S3 bucket: {}", e.getMessage());
        }
    }
}
