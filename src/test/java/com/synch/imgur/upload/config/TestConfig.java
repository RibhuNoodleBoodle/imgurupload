package com.synch.imgur.upload.config;
import com.synch.imgur.upload.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestConfig {
    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }
}

