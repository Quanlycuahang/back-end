package com.example.DoantotnghiepIJ.service;


import com.example.DoantotnghiepIJ.dto.CategoryDto.*;
import com.example.DoantotnghiepIJ.entity.Category;
import com.example.DoantotnghiepIJ.exception.*;
import com.example.DoantotnghiepIJ.mapper.CategoryMapper;
import com.example.DoantotnghiepIJ.repository.CategoryRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // CREATE
    public CategoryResponseDto create(CreateCategoryDto dto) {

        categoryRepository.findByName(dto.getName())
                .ifPresent(c -> {
                    throw new BadRequestException("Category already exists");
                });

        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());

        // set parent nếu có
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent not found"));
            category.setParent(parent);
        }

        return CategoryMapper.toDto(categoryRepository.save(category));
    }
//    get tree (Danh mục cha con)
    public List<CategoryResponseDto> getTree() {

        List<Category> roots = categoryRepository.findByParentIsNullAndDeletedFalse();

        return roots.stream()
                .map(CategoryMapper::toDto)
                .toList();
    }
    // GET ALL
    public Page<Category> getCategories(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        // 🔥 CASE 1: không search
        if (keyword == null || keyword.trim().isEmpty()) {
            return categoryRepository.findByDeletedFalse(pageable);
        }

        // 🔥 CASE 2: có search
        return categoryRepository.findByDeletedFalseAndNameContainingIgnoreCase(keyword, pageable);
}

        // GET BY ID
    public CategoryResponseDto getById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        return CategoryMapper.toDto(category);
    }

    // UPDATE
    public CategoryResponseDto update(Long id, UpdateCategoryDto dto) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        CategoryMapper.updateEntity(category, dto);

        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    // DELETE (soft)
    public void delete(Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (Boolean.TRUE.equals(category.getDeleted())) {
            throw new BadRequestException("Category already deleted");
        }

        category.setDeleted(true);
        categoryRepository.save(category);
    }

//    dashboard
public CategoryStatsResponse getCategoryStats() {
    long total = categoryRepository.countAllCategories();
    long empty = categoryRepository.countEmptyCategories();

    return CategoryStatsResponse.builder()
            .totalCategories(total)
            .emptyCategories(empty)
            .build();
}
}
