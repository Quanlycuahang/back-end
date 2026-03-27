package com.example.DoantotnghiepIJ.controller.admin;

import com.example.DoantotnghiepIJ.dto.CategoryDto.CreateCategoryDto;
import com.example.DoantotnghiepIJ.dto.CategoryDto.UpdateCategoryDto;
import com.example.DoantotnghiepIJ.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    //  CREATE (có parentId)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateCategoryDto dto) {
        return ResponseEntity.ok(categoryService.create(dto));
    }

    //  GET ALL (flat list)
    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    //  GET TREE (QUAN TRỌNG)
    @GetMapping("/tree")
    public ResponseEntity<?> getTree() {
        return ResponseEntity.ok(categoryService.getTree());
    }

    //  GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getById(id));
    }

    //  UPDATE (có thể đổi parent)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody UpdateCategoryDto dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    //  DELETE (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok("Deleted category with id: " + id);
    }
}