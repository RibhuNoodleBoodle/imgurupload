package com.synch.imgur.upload.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey; // Secret key used to sign JWT tokens

    @Value("${jwt.expiration}")
    private long expirationTime; // Expiration time in milliseconds (e.g., 1 hour)

    // Generate JWT Token for a user
    public String generateToken(String username) {
        return JWT.create()
                .withSubject(username)                // The subject of the token (usually the username)
                .withIssuedAt(new Date())             // The issued date
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))  // Expiration time
                .sign(Algorithm.HMAC256(secretKey));  // Sign the token using HMAC256 and the secret key
    }

    // Extract username (subject) from the token
    public String extractUsername(String token) {
        return extractClaim(token, DecodedJWT::getSubject);
    }

    // Extract claims (useful for additional information in the future)
    private <T> T extractClaim(String token, java.util.function.Function<DecodedJWT, T> claimsResolver) {
        DecodedJWT decodedJWT = decodeToken(token);
        return claimsResolver.apply(decodedJWT);
    }

    // Decode and validate the token
    private DecodedJWT decodeToken(String token) {
        return JWT.require(Algorithm.HMAC256(secretKey))
                .build()
                .verify(token);  // Verify the token and decode it
    }

    // Validate the token to check if it is expired or tampered with
    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Check if the token has expired
    private boolean isTokenExpired(String token) {
        return extractClaim(token, DecodedJWT::getExpiresAt).before(new Date());
    }
}
