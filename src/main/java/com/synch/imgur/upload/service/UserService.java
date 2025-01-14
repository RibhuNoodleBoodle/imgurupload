package com.synch.imgur.upload.service;


import com.synch.imgur.upload.models.dto.ImageResponseDTO;
import com.synch.imgur.upload.models.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO registerUser(UserDTO userDTO);
    UserDTO getUserByUsername(String username);
    ImageResponseDTO associateImageWithUser(String username, String imageHash, String deleteHash, String imageUrl);
    List<ImageResponseDTO> getUserImages(String username);
    ImageResponseDTO getUserImage(String username, String imageHash);
    String getImageDeleteHash(String username, String imageHash);
    void removeImageAssociation(String username, String imageHash);
}
