package ru.kharevich.imageservice.util.properties;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "app.s3")
@Getter
@NoArgsConstructor
@Setter
@Component
public class S3Properties {

    private String endpoint;

    private String bucketName;

    private String accessKey;

    private String secretKey;

    private String region;
}
