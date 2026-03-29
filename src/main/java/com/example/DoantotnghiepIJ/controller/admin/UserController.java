package com.example.DoantotnghiepIJ.controller.admin;

import com.example.DoantotnghiepIJ.Enum.UserStatus;
import com.example.DoantotnghiepIJ.dto.UserDto.UserStatisticsDto;
import com.example.DoantotnghiepIJ.entity.User;
import com.example.DoantotnghiepIJ.service.MenuItemService;
import com.example.DoantotnghiepIJ.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "User API", description = "Quản lý user")
public class UserController {

    private final UserService userService;
    private final MenuItemService menuItemService;
    public UserController(UserService userService, MenuItemService menuItemService) {
        this.userService = userService;
        this.menuItemService = menuItemService;
    }

    @Operation(summary = "Tạo user")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @Operation(summary = "Sửa user")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "Lấy thông tin user")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Lấy danh sách user")
    @GetMapping
    public ResponseEntity<?> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                userService.getUsers(keyword, status, page, size)
        );
    }
    @Operation(summary = "Xóa user")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Deleted user with id: " + id);
    }


    @Operation(summary = "Update Status user")
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam UserStatus status
    ) {
        return ResponseEntity.ok(userService.updateUserStatus(id, status));
    }

//    upload ảnh
    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file
    ) {

        Long userId = 1L; // TODO: lấy từ JWT

        String url = userService.uploadAvatar(userId, file);

        return ResponseEntity.ok(Map.of(
                "avatarUrl", url
        ));
    }
//    Dashboard thống kê user
    @GetMapping("/statistics")
    public UserStatisticsDto getStatistics() {
        return userService.getUserStatistics();
    }
//    API nhập kho
    @PutMapping("/{id}/increase-stock")
    public ResponseEntity<?> increaseStock(
            @PathVariable UUID id,
            @RequestParam int amount
    ) {
        menuItemService.increaseStock(id, amount);
        return ResponseEntity.ok("Nhập kho thành công");
    }
//    API xuất kho
    @PutMapping("/{id}/decrease-stock")
    public ResponseEntity<?> decreaseStock(
            @PathVariable UUID id,
            @RequestParam int amount
    ) {
        menuItemService.decreaseStock(id, amount);
        return ResponseEntity.ok("Xuất kho thành công");
    }
}