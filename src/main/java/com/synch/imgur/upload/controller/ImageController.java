package com.synch.imgur.upload.controller;

import com.synch.imgur.upload.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("image") MultipartFile file) {
        return imageService.uploadImage(file);
    }

    @GetMapping("/{imageId}")
    public ResponseEntity<String> viewImage(@PathVariable String imageId) {
        return imageService.viewImage(imageId);
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<String> deleteImage(@PathVariable String imageId) {
        return imageService.deleteImage(imageId);
    }
}
