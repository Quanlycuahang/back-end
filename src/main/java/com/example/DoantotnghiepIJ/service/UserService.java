package com.example.DoantotnghiepIJ.service;

import com.example.DoantotnghiepIJ.dto.UserDto.UserStatisticsDto;
import com.example.DoantotnghiepIJ.entity.User;
import com.example.DoantotnghiepIJ.repository.UserRepository;
import com.example.DoantotnghiepIJ.exception.NotFoundException;
import com.example.DoantotnghiepIJ.exception.BadRequestException;
import com.example.DoantotnghiepIJ.Enum.UserStatus;

import com.example.DoantotnghiepIJ.validate.UtilsValidate;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.ValidationUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
    }

    //  GET ALL
    public Page<User> getUsers(
            String keyword,
            UserStatus status,
            int page,
            int size
    ) {
        System.out.println("Keyword: " + keyword);
        System.out.println("Status: " + status);
        System.out.println("Total users: " + userRepository.count());
        System.out.println("Page: " + page);
        System.out.println("Size: " + size);
        if (keyword != null) {
            keyword = keyword.toLowerCase();
        }

        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        Pageable pageable = PageRequest.of(page, size); // ❌ không sort

        return userRepository.searchUsers(keyword, status, pageable);
    }

    //  GET BY ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    //  CREATE
    public User createUser(User user) {

        // validate trước
        UtilsValidate.validateEmail(user.getEmail());
        UtilsValidate.validatePhone(user.getPhone());

        // check trùng
        userRepository.findByEmail(user.getEmail())
                .ifPresent(u -> {
                    throw new BadRequestException("Email already exists");
                });

        userRepository.findByPhone(user.getPhone())
                .ifPresent(u -> {
                    throw new BadRequestException("Phone already exists");
                });

        // hash password
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));

        // default status
        if (user.getStatus() == null) {
            user.setStatus(UserStatus.ACTIVE);
        }

        return userRepository.save(user);
    }

    //  UPDATE
    public User updateUser(Long id, User request) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        // check delete
        if (Boolean.TRUE.equals(existingUser.getDeleted())) {
            throw new BadRequestException("User has been deleted");
        }

        // check status
        if (existingUser.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("User is disabled");
        }

        // EMAIL
        if (request.getEmail() != null &&
                !request.getEmail().equals(existingUser.getEmail())) {

            UtilsValidate.validateEmail(request.getEmail());

            userRepository.findByEmail(request.getEmail())
                    .ifPresent(u -> {
                        throw new BadRequestException("Email already exists");
                    });

            existingUser.setEmail(request.getEmail());
        }

        // PHONE
        if (request.getPhone() != null &&
                !request.getPhone().equals(existingUser.getPhone())) {

            UtilsValidate.validatePhone(request.getPhone());

            userRepository.findByPhone(request.getPhone())
                    .ifPresent(u -> {
                        throw new BadRequestException("Phone already exists");
                    });

            existingUser.setPhone(request.getPhone());
        }

        // update field
        if (request.getFullName() != null) {
            existingUser.setFullName(request.getFullName());
        }

        if (request.getGender() != null) {
            existingUser.setGender(request.getGender());
        }

        if (request.getDateOfBirth() != null) {
            existingUser.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getStatus() != null) {
            existingUser.setStatus(request.getStatus());
        }

        if (request.getPasswordHash() != null) {
            existingUser.setPasswordHash(passwordEncoder.encode(request.getPasswordHash()));
        }

        existingUser.setUpdatedAt(java.time.LocalDateTime.now());

        return userRepository.save(existingUser);
    }

    //  DELETE (soft delete)
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BadRequestException("User already deleted");
        }

        user.setDeleted(true);
        user.setUpdatedAt(java.time.LocalDateTime.now());

        userRepository.save(user);
    }

    //  UPDATE STATUS (lock/unlock)
    public User updateUserStatus(Long id, UserStatus status) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BadRequestException("User has been deleted");
        }
        user.setStatus(status);
        user.setUpdatedAt(java.time.LocalDateTime.now());

        return userRepository.save(user);
    }
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        //  Nếu bị khóa
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new BadRequestException("User is disabled");
        }
        // check password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("Invalid password");
        }

        return user;
    }
//    UPLOAD ẢNH
    @Transactional
    public String uploadAvatar(Long userId, MultipartFile file) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // validate
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new RuntimeException("File must be image");
        }

        // delete ảnh cũ (an toàn)
        if (user.getPublicId() != null && !user.getPublicId().isBlank()) {
            try {
                cloudinaryService.delete(user.getPublicId());
            } catch (Exception e) {
                System.out.println("Skip delete old image");
            }
        }

        // upload ảnh mới
        Map result = cloudinaryService.upload(file);

        String url = result.get("secure_url").toString();
        String publicId = result.get("public_id").toString();

        user.setAvatarUrl(url);
        user.setPublicId(publicId);

        userRepository.save(user);

        return url;
    }
//    dashboard thống kê user
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