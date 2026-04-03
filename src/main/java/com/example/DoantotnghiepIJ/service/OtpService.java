package com.example.DoantotnghiepIJ.service;

import com.example.DoantotnghiepIJ.Enum.OtpType;
import com.example.DoantotnghiepIJ.entity.User;
import com.example.DoantotnghiepIJ.exception.BadRequestException;
import com.example.DoantotnghiepIJ.repository.UserRepository;
import com.example.DoantotnghiepIJ.validate.UtilsValidate;
import lombok.RequiredArgsConstructor;
//import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    // 🔥 SEND OTP
    public void sendOtp(String email, OtpType type) {

        UtilsValidate.validateEmail(email);

        User user = userRepository.findByEmail(email)
                .orElse(new User());

        // 🔥 chống spam 30s
        if (user.getOtpSentAt() != null &&
                user.getOtpSentAt().isAfter(LocalDateTime.now().minusSeconds(30))) {

            throw new BadRequestException("Vui lòng đợi 30 giây để gửi lại OTP");
        }

        // tạo OTP
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        user.setEmail(email);
        user.setOtp(otp);
        user.setOtpType(type);
        user.setOtpSentAt(LocalDateTime.now());
        user.setOtpExpiredAt(LocalDateTime.now().plusMinutes(5));

        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);
    }

    // 🔥 VERIFY OTP
    public void verifyOtp(String email, String otp, OtpType type) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email không tồn tại"));

        if (user.getOtp() == null) {
            throw new BadRequestException("Chưa gửi OTP");
        }

        if (!user.getOtp().equals(otp)) {
            throw new BadRequestException("OTP không đúng");
        }

        if (user.getOtpExpiredAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP đã hết hạn");
        }

        if (user.getOtpType() != type) {
            throw new BadRequestException("OTP không hợp lệ");
        }

        // clear OTP sau khi dùng
        user.setOtp(null);
        user.setOtpExpiredAt(null);
        user.setOtpType(null);

        userRepository.save(user);
    }
}