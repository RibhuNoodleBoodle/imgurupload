package com.synch.imgur.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "imgur")
public class ImgurProperties {
    private String clientId;
    private String clientSecret;
    private String uploadEndpoint = "https://api.imgur.com/3/image";
    private String deleteEndpoint = "https://api.imgur.com/3/image/{imageHash}";
    private String imageEndpoint = "https://api.imgur.com/3/image/{imageHash}";
    private Integer uploadTimeout = 5000;
    private Integer connectionTimeout = 3000;
}
