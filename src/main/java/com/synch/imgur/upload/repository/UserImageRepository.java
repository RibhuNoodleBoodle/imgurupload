package com.synch.imgur.upload.repository;


import com.synch.imgur.upload.models.user.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserImageRepository extends JpaRepository<UserImage, Long> {
    List<UserImage> findByUserUsername(String username);
    Optional<UserImage> findByUserUsernameAndImageHash(String username, String imageHash);
}
