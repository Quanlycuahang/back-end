package com.example.DoantotnghiepIJ.controller.admin;

import com.example.DoantotnghiepIJ.dto.Menu.MenuItemDto;
import com.example.DoantotnghiepIJ.entity.MenuItem;
import com.example.DoantotnghiepIJ.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/menu-items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    // ================= CREATE =================
    @PostMapping
    public ResponseEntity<?> create(@RequestBody MenuItemDto dto) {
        return ResponseEntity.ok(menuItemService.create(dto));
    }

    // ================= GET ALL (paging + sort) =================
    @GetMapping
    public ResponseEntity<Page<MenuItem>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(
                menuItemService.getAll(page, size, sortBy, sortDir)
        );
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(menuItemService.getById(id));
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> update(
            @PathVariable UUID id,
            @RequestBody MenuItemDto dto
    ) {
        return ResponseEntity.ok(menuItemService.update(id, dto));
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        menuItemService.delete(id);
        return ResponseEntity.ok("Deleted successfully");
    }

    // ================= TOGGLE STATUS =================
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable UUID id) {
        menuItemService.toggleStatus(id);
        return ResponseEntity.ok("Status updated");
    }

    // ================= SEARCH + FILTER =================
    @GetMapping("/search")
    public ResponseEntity<Page<MenuItem>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                menuItemService.search(keyword, isActive, categoryId, page, size)
        );
    }

    // ================= UPLOAD IMAGES =================
    @PostMapping("/{id}/upload-images")
    public ResponseEntity<?> uploadImages(
            @PathVariable UUID id,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) {

        menuItemService.uploadImages(id, thumbnail, images);

        return ResponseEntity.ok("Upload thành công");
    }
}