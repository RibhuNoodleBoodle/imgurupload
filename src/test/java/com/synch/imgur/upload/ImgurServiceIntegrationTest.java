package com.synch.imgur.upload;


import com.synch.imgur.upload.exceptions.ImgurApiException;
import com.synch.imgur.upload.service.ImgurService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class ImgurServiceIntegrationTest {

    @Autowired
    private ImgurService imgurService;

    @Test
    void testImgurIntegration() {
        // Create a test image (a simple black pixel)
        byte[] imageContent = {
                (byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, // PNG signature
                // ... more PNG file bytes for a 1x1 black pixel
        };

        MockMultipartFile file = new MockMultipartFile(
                "test-image.png",
                "test-image.png",
                "image/png",
                imageContent
        );

        try {
            // Upload the image
            Map<String, String> uploadResponse = imgurService.uploadImage(file);

            assertNotNull(uploadResponse);
            assertNotNull(uploadResponse.get("imageHash"));
            assertNotNull(uploadResponse.get("deleteHash"));
            assertNotNull(uploadResponse.get("imageUrl"));

            String imageHash = uploadResponse.get("imageHash");
            String deleteHash = uploadResponse.get("deleteHash");

            // Get image info
            Map<String, String> imageInfo = imgurService.getImage(imageHash);
            assertNotNull(imageInfo);
            assertEquals("image/png", imageInfo.get("type"));

            // Delete the image
            imgurService.deleteImage(deleteHash);

            // Verify deletion - this should throw an exception
            assertThrows(ImgurApiException.class, () ->
                    imgurService.getImage(imageHash)
            );

        } catch (Exception e) {
            fail("Integration test failed: " + e.getMessage());
        }
    }
}