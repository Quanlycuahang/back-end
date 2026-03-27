package com.example.DoantotnghiepIJ.controller.admin;

import com.example.DoantotnghiepIJ.dto.Menu.MenuItemDto;
import com.example.DoantotnghiepIJ.entity.MenuItem;
import com.example.DoantotnghiepIJ.service.MenuItemService;
import lombok.RequiredArgsConstructor;
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

    // CREATE
    @PostMapping
    public MenuItem create(@RequestBody MenuItemDto dto) {
        return menuItemService.create(dto);
    }

    // GET ALL
    @GetMapping
    public List<MenuItem> getAll() {
        return menuItemService.getAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public MenuItem getById(@PathVariable UUID id) {
        return menuItemService.getById(id);
    }

    // UPDATE
    @PutMapping("/{id}")
    public MenuItem update(@PathVariable UUID id,
                           @RequestBody MenuItemDto dto) {
        return menuItemService.update(id, dto);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id) {
        menuItemService.delete(id);
        return "Deleted successfully";
    }

    //upload nhieeuf anh
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