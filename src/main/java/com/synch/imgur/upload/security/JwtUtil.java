package com.synch.imgur.upload.security;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationTime;

    public String generateToken(String phoneNumber) {
        return JWT.create()
                .withSubject(phoneNumber)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationTime))
                .sign(Algorithm.HMAC256(secretKey));
    }

    public String extractPhoneNumber(String token) {
        return JWT.decode(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return JWT.decode(token).getExpiresAt().before(new Date());
    }

    public boolean validateToken(String token, String phoneNumber) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secretKey))
                    .build();
            DecodedJWT decodedJWT = verifier.verify(token);
            return decodedJWT.getSubject().equals(phoneNumber) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}
