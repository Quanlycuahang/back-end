package com.example.DoantotnghiepIJ.service;

import com.example.DoantotnghiepIJ.dto.UserDto.UserStatisticsDto;
import com.example.DoantotnghiepIJ.entity.User;
import com.example.DoantotnghiepIJ.repository.UserRepository;
import com.example.DoantotnghiepIJ.exception.NotFoundException;
import com.example.DoantotnghiepIJ.exception.BadRequestException;
import com.example.DoantotnghiepIJ.Enum.UserStatus;
import com.example.DoantotnghiepIJ.validate.UtilsValidate;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
    }

    // ===================== GET ALL =====================
    public Page<User> getUsers(String keyword, UserStatus status, int page, int size) {

        if (page < 0) throw new BadRequestException("Page must >= 0");
        if (size <= 0 || size > 100) throw new BadRequestException("Size must be 1-100");

        if (keyword != null) {
            keyword = keyword.trim().toLowerCase();
            if (keyword.isEmpty()) keyword = null;
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return userRepository.searchUsers(keyword, status, pageable);
    }

    // ===================== GET BY ID =====================
    public User getUserById(Long id) {
        if (id == null) throw new BadRequestException("Id is required");

        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    // ===================== CREATE =====================
    public User createUser(User user) {

        if (user == null) throw new BadRequestException("User is required");

        // ===== REQUIRED =====
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        // ===== TRIM =====
        String email = user.getEmail().trim().toLowerCase();
        String phone = user.getPhone() != null ? user.getPhone().trim() : null;
        String password = user.getPasswordHash().trim();

        // ===== VALIDATE =====
        UtilsValidate.validateEmail(email);
        if (phone != null) UtilsValidate.validatePhone(phone);
        UtilsValidate.validatePassword(password);

        // ===== CHECK DUPLICATE =====
        userRepository.findByEmail(email)
                .ifPresent(u -> { throw new BadRequestException("Email already exists"); });

        if (phone != null) {
            userRepository.findByPhone(phone)
                    .ifPresent(u -> { throw new BadRequestException("Phone already exists"); });
        }

        // ===== SET DATA =====
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));

        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // ===================== UPDATE =====================
    public User updateUser(Long id, User request) {

        if (id == null) throw new BadRequestException("Id is required");

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (Boolean.TRUE.equals(existingUser.getDeleted())) {
            throw new BadRequestException("User has been deleted");
        }

        if (existingUser.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("User is disabled");
        }

        // ===== EMAIL =====
        if (request.getEmail() != null) {
            String email = request.getEmail().trim().toLowerCase();

            if (!email.equals(existingUser.getEmail())) {

                UtilsValidate.validateEmail(email);

                userRepository.findByEmail(email)
                        .ifPresent(u -> { throw new BadRequestException("Email already exists"); });

                existingUser.setEmail(email);
            }
        }

        // ===== PHONE =====
        if (request.getPhone() != null) {
            String phone = request.getPhone().trim();

            if (!phone.equals(existingUser.getPhone())) {

                UtilsValidate.validatePhone(phone);

                userRepository.findByPhone(phone)
                        .ifPresent(u -> { throw new BadRequestException("Phone already exists"); });

                existingUser.setPhone(phone);
            }
        }

        // ===== OTHER FIELDS =====
        if (request.getFullName() != null) {
            existingUser.setFullName(request.getFullName().trim());
        }

        if (request.getGender() != null) {
            existingUser.setGender(request.getGender());
        }

        if (request.getDateOfBirth() != null) {
            if (request.getDateOfBirth().isAfter(LocalDateTime.now())) {
                throw new BadRequestException("Date of birth is invalid");
            }
            existingUser.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getStatus() != null) {
            existingUser.setStatus(request.getStatus());
        }

        // ===== PASSWORD =====
        if (request.getPasswordHash() != null) {

            String password = request.getPasswordHash().trim();

            UtilsValidate.validatePassword(password);

            existingUser.setPasswordHash(passwordEncoder.encode(password));
        }

        existingUser.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    // ===================== DELETE =====================
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BadRequestException("User already deleted");
        }

        user.setDeleted(true);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    // ===================== STATUS =====================
    public User updateUserStatus(Long id, UserStatus status) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BadRequestException("User has been deleted");
        }

        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    // ===================== LOGIN =====================
    public User login(String email, String password) {

        if (email == null || password == null) {
            throw new BadRequestException("Email and password are required");
        }

        email = email.trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("User is disabled");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("Invalid password");
        }

        return user;
    }

    // ===================== UPLOAD AVATAR =====================
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File too large (max 5MB)");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new BadRequestException("File must be image");
        }

        if (user.getPublicId() != null && !user.getPublicId().isBlank()) {
            try {
                cloudinaryService.delete(user.getPublicId());
            } catch (Exception ignored) {}
        }

        Map result = cloudinaryService.upload(file);

        String url = result.get("secure_url").toString();
        String publicId = result.get("public_id").toString();

        user.setAvatarUrl(url);
        user.setPublicId(publicId);

        userRepository.save(user);

        return url;
    }

    // ===================== STATISTICS =====================
    public UserStatisticsDto getUserStatistics() {

        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long newUsersToday = userRepository.countUsersCreatedToday(LocalDate.now());

        return UserStatisticsDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .newUsersToday(newUsersToday)
                .build();
    }
}