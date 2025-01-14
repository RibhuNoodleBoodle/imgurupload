package com.synch.imgur.upload.controller;

import com.synch.imgur.upload.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest
public class ImageControllerTests {

    private MockMvc mockMvc;

    @InjectMocks
    private ImageController imageController;

    @Mock
    private ImageService imageService;

    @Value("${imgur.client.id}")
    private String imgurClientId;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController).build();
    }

    // Helper method to generate a black block image (640x480)
    private byte[] generateBlackBlockImage() throws IOException {
        BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(image, "jpeg", baos);
        return baos.toByteArray();
    }

    @Test
    public void testUploadImage() throws Exception {
        // Generate the black block image
        byte[] blackBlockImage = generateBlackBlockImage();

        // Mock the ImageService to simulate the upload response
        when(imageService.uploadImage(org.mockito.ArgumentMatchers.any()))
                .thenReturn(org.springframework.http.ResponseEntity.status(HttpStatus.OK).body("Upload successful"));

        // Perform the test upload
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.multipart("/images/upload")
                        .file("file", blackBlockImage)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string("Upload successful"))
                .andReturn();

        // Optionally print the result for verification
        System.out.println("Response: " + result.getResponse().getContentAsString());
    }
}

