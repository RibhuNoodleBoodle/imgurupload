package com.synch.imgur.upload.service;
import com.synch.imgur.upload.exceptions.DuplicateResourceException;
import com.synch.imgur.upload.exceptions.UserNotFoundException;
import com.synch.imgur.upload.models.dto.ImageResponseDTO;
import com.synch.imgur.upload.models.dto.UserDTO;
import com.synch.imgur.upload.models.user.User;
import com.synch.imgur.upload.models.user.UserImage;
import com.synch.imgur.upload.repository.UserImageRepository;
import com.synch.imgur.upload.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDTO registerUser(UserDTO userDTO) {
        log.info("Registering new user with username: {}", userDTO.getUsername());

        if (userRepository.existsByUsername(userDTO.getUsername())) {
            try {
                throw new DuplicateResourceException("Username already exists");
            } catch (DuplicateResourceException e) {
                throw new RuntimeException(e);
            }
        }
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            try {
                throw new DuplicateResourceException("Email already exists");
            } catch (DuplicateResourceException e) {
                throw new RuntimeException(e);
            }
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setEmail(userDTO.getEmail());

        User savedUser = userRepository.save(user);
        log.info("Successfully registered user: {}", savedUser.getUsername());

        return convertToDTO(savedUser);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        log.info("Retrieving user details for username: {}", username);
        try {
            return userRepository.findByUsername(username)
                    .map(this::convertToDTO)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public ImageResponseDTO associateImageWithUser(String username, String imageHash,
                                                   String deleteHash, String imageUrl) {
        log.info("Associating image {} with user {}", imageHash, username);

        User user = null;
        try {
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }

        UserImage userImage = new UserImage();
        userImage.setUser(user);
        userImage.setImageHash(imageHash);
        userImage.setDeleteHash(deleteHash);
        userImage.setImageUrl(imageUrl);

        userImage = userImageRepository.save(userImage);
        log.info("Successfully associated image with user");

        return convertToImageDTO(userImage);
    }

    @Override
    public List<ImageResponseDTO> getUserImages(String username) {
        log.info("Retrieving all images for user: {}", username);
        return userImageRepository.findByUserUsername(username).stream()
                .map(this::convertToImageDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ImageResponseDTO getUserImage(String username, String imageHash) {
        log.info("Retrieving specific image {} for user {}", imageHash, username);
        return userImageRepository.findByUserUsernameAndImageHash(username, imageHash)
                .map(this::convertToImageDTO)
                .orElse(null);
    }

    @Override
    public String getImageDeleteHash(String username, String imageHash) {
        log.info("Retrieving delete hash for image {} of user {}", imageHash, username);
        return userImageRepository.findByUserUsernameAndImageHash(username, imageHash)
                .map(UserImage::getDeleteHash)
                .orElse(null);
    }

    @Override
    @Transactional
    public void removeImageAssociation(String username, String imageHash) {
        log.info("Removing image association {} for user {}", imageHash, username);
        userImageRepository.findByUserUsernameAndImageHash(username, imageHash)
                .ifPresent(userImageRepository::delete);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    private ImageResponseDTO convertToImageDTO(UserImage userImage) {
        return ImageResponseDTO.builder()
                .imageHash(userImage.getImageHash())
                .imageUrl(userImage.getImageUrl())
                .uploadDate(userImage.getUploadDate())
                .build();
    }
}
