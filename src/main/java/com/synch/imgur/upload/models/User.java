package com.synch.imgur.upload.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = false, nullable = true)
    private String email;

    @ElementCollection
    @CollectionTable(name = "user_images", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "image_id")
    private List<String> imageIds;
}

