package com.example.DoantotnghiepIJ.controller.client;

import com.example.DoantotnghiepIJ.dto.login.LoginRequest;
import com.example.DoantotnghiepIJ.dto.login.LoginResponse;
import com.example.DoantotnghiepIJ.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 🔐 LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse response) {

        // ✅ Lấy device + IP
        String userAgent = httpRequest.getHeader("User-Agent");
        String clientIp = httpRequest.getRemoteAddr();

        return ResponseEntity.ok(
                authService.login(request, response, userAgent, clientIp)
        );
    }

    // 🔄 REFRESH TOKEN
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request,
                                          HttpServletResponse response) {

        String userAgent = request.getHeader("User-Agent");
        String clientIp = request.getRemoteAddr();

        LoginResponse result = authService.refreshToken(
                request,
                response,
                userAgent,
                clientIp
        );

        return ResponseEntity.ok(result);
    }
}