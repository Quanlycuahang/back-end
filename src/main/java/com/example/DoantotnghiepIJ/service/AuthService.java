package com.example.DoantotnghiepIJ.service;

import com.example.DoantotnghiepIJ.entity.RefreshToken;
import com.example.DoantotnghiepIJ.exception.ApiException;
import com.example.DoantotnghiepIJ.Enum.ErrorCode;
import com.example.DoantotnghiepIJ.Enum.UserStatus;
import com.example.DoantotnghiepIJ.dto.login.LoginRequest;
import com.example.DoantotnghiepIJ.dto.login.LoginResponse;
import com.example.DoantotnghiepIJ.entity.User;
import com.example.DoantotnghiepIJ.repository.RefreshTokenRepository;
import com.example.DoantotnghiepIJ.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    // ================= LOGIN =================
    public LoginResponse login(LoginRequest request,
                               HttpServletResponse response,
                               String userAgent,
                               String clientIp) {

        User user = userRepository.findByEmailAndDeletedFalse(request.getEmail())
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.INVALID_PASSWORD);
        }

        // access token
        String accessToken = jwtService.generateAccessToken(user);

        // refresh token
        String refreshToken = refreshTokenService.createRefreshToken(
                user, userAgent, clientIp
        );

        // set cookie
        response.addHeader("Set-Cookie",
                "refreshToken=" + refreshToken +
                        "; HttpOnly; Path=/auth; Max-Age=604800; SameSite=Strict");

        return LoginResponse.builder()
                .message("Login successful")
                .accessToken(accessToken)
                .fullName(user.getFullName())
                .build();
    }

    // ================= REFRESH =================
    public LoginResponse refreshToken(HttpServletRequest request,
                                      HttpServletResponse response,
                                      String userAgent,
                                      String clientIp) {

        String refreshToken = extractRefreshTokenFromCookie(request);

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_TOKEN));

        if (token.isRevoked() || token.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.TOKEN_EXPIRED);
        }

        User user = token.getUser();

        // check user
        if (user.getStatus() != UserStatus.ACTIVE || Boolean.TRUE.equals(user.getDeleted())) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED);
        }

        // revoke old token (rotation)
        token.setRevoked(true);
        refreshTokenRepository.save(token);

        // create new refresh token
        String newRefreshToken = refreshTokenService.createRefreshToken(
                user, userAgent, clientIp
        );

        // set new cookie
        response.addHeader("Set-Cookie",
                "refreshToken=" + newRefreshToken +
                        "; HttpOnly; Path=/auth; Max-Age=604800; SameSite=Strict");

        // new access token
        String newAccessToken = jwtService.generateAccessToken(user);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    // ================= HELPER =================
    private String extractRefreshTokenFromCookie(HttpServletRequest request) {

        if (request.getCookies() == null) {
            throw new ApiException(ErrorCode.INVALID_TOKEN);
        }

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        throw new ApiException(ErrorCode.INVALID_TOKEN);
    }
}