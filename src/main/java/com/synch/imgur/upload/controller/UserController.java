package com.synch.imgur.upload.controller;
import com.synch.imgur.upload.exceptions.DuplicateResourceException;
import com.synch.imgur.upload.models.dto.ApiResponse;
import com.synch.imgur.upload.models.dto.UserDTO;
import com.synch.imgur.upload.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDTO>> registerUser(@RequestBody @Validated UserDTO userDTO) {
        log.info("Received registration request for username: {}", userDTO.getUsername());

        try {
            UserDTO registeredUser = userService.registerUser(userDTO);
            return ResponseEntity.ok(new ApiResponse<>(true, "User registered successfully", registeredUser));

        } catch (DuplicateResourceException e) {
            log.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("Unexpected error during registration: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Registration failed: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable String username) {
        log.info("Retrieving user details for: {}", username);

        try {
            UserDTO userDTO = userService.getUserByUsername(username);
            return ResponseEntity.ok(new ApiResponse<>(true, "User retrieved successfully", userDTO));

        } catch (Exception e) {
            log.error("Error retrieving user: ", e);
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Failed to retrieve user: " + e.getMessage(), null));
        }
    }
}
