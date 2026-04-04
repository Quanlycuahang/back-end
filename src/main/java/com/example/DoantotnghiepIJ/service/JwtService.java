package com.example.DoantotnghiepIJ.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import com.example.DoantotnghiepIJ.entity.User;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private final String SECRET_KEY = "gK8fL2p9QwXz7VbN3mHcT6yR1uD4sE8jKp0ZxWqA9fL2M5nB";

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 phút
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
}