    package com.synch.imgur.upload.controller;

    import com.synch.imgur.upload.config.TestConfig;
    import com.synch.imgur.upload.service.ImageService;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
    import org.springframework.boot.test.mock.mockito.MockBean;
    import org.springframework.http.MediaType;
    import org.springframework.http.ResponseEntity;
    import org.springframework.mock.web.MockMultipartFile;
    import org.springframework.test.context.ContextConfiguration;
    import org.springframework.test.web.servlet.MockMvc;
    import org.springframework.beans.factory.annotation.Autowired;

    import static org.mockito.Mockito.when;
    import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
    import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

    @ContextConfiguration(classes = TestConfig.class)
    @WebMvcTest(ImageController.class)
    class ImageControllerTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ImageService imageService;

        @Value("${imgur.client.id}")  // Use your Imgur Client-ID
        private String clientId;

        @Test
        void testUploadImage() throws Exception {
            MockMultipartFile testImage = new MockMultipartFile(
                    "image",
                    "test.jpg",
                    MediaType.IMAGE_JPEG_VALUE,
                    "Test Image Content".getBytes()
            );

            when(imageService.uploadImage(testImage)).thenReturn(ResponseEntity.ok("Image uploaded successfully"));

            mockMvc.perform(multipart("/images/upload")
                            .file(testImage)
                            .header("Client-ID", clientId))
                    .andExpect(status().isOk());
        }

        @Test
        void testViewImage() throws Exception {
            String imageId = "testImageId";
            when(imageService.viewImage(imageId)).thenReturn(ResponseEntity.ok("Image details"));

            mockMvc.perform(get("/images/{imageId}", imageId)
                            .header("Authorization", "Client-ID " + clientId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Image details"));
        }

        @Test
        void testDeleteImage() throws Exception {
            String imageId = "testImageId";
            when(imageService.deleteImage(imageId)).thenReturn(ResponseEntity.ok("Image deleted successfully"));

            mockMvc.perform(delete("/images/{imageId}", imageId)
                            .header("Authorization", "Client-ID " + clientId))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Image deleted successfully"));
        }
    }
