package com.example.DoantotnghiepIJ.service;


import com.example.DoantotnghiepIJ.dto.CategoryDto.CategoryResponseDto;
import com.example.DoantotnghiepIJ.dto.CategoryDto.CategorySimpleDto;
import com.example.DoantotnghiepIJ.dto.CategoryDto.CreateCategoryDto;
import com.example.DoantotnghiepIJ.dto.CategoryDto.UpdateCategoryDto;
import com.example.DoantotnghiepIJ.entity.Category;
import com.example.DoantotnghiepIJ.exception.*;
import com.example.DoantotnghiepIJ.mapper.CategoryMapper;
import com.example.DoantotnghiepIJ.repository.CategoryRepository;

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
    public List<CategorySimpleDto> getAll() {
        return categoryRepository.findByDeletedFalse()
                .stream()
                .map(CategoryMapper::toSimpleDto)
                .toList();
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
}
