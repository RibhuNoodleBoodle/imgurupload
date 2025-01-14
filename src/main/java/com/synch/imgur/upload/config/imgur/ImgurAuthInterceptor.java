package com.synch.imgur.upload.config.imgur;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImgurAuthInterceptor implements ClientHttpRequestInterceptor {

    private final ImgurProperties imgurProperties;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {

        log.debug("Adding Imgur authentication headers to request");

        // Add the required Imgur API headers
        request.getHeaders().set(HttpHeaders.AUTHORIZATION,
                "Client-ID " + imgurProperties.getClientId());

        return execution.execute(request, body);
    }
}
