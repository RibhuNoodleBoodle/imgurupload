package com.synch.imgur.upload.controller;

import com.synch.imgur.upload.models.LoginRequest;
import com.synch.imgur.upload.models.User;
import com.synch.imgur.upload.security.JwtUtil;
import com.synch.imgur.upload.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userService.findUserByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username already exists.");
        }
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        // Authenticate the user (simplified for demonstration)
        // You can perform authentication using UsernamePasswordAuthenticationToken here

        String username = loginRequest.getUsername();
        String token = jwtUtil.generateToken(username);

        // Create a cookie to store the JWT token
        Cookie cookie = new Cookie("JWT", token);
        cookie.setHttpOnly(true); // Secure flag (set to true in production)
        cookie.setMaxAge(86400); // Token expiration (1 day)
        cookie.setPath("/"); // Make it available for all endpoints

        // Add cookie to the response
        response.addCookie(cookie);

        return ResponseEntity.status(HttpStatus.OK).body("Login successful");
    }


    @GetMapping
    public ResponseEntity<List<User>> listAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}
