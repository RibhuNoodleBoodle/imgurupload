package com.synch.imgur.upload.service;
import com.synch.imgur.upload.models.User;
import java.util.List;

public interface UserService {
    User registerUser(User user);
    User findUserByUsername(String username);
    List<User> getAllUsers();
}

