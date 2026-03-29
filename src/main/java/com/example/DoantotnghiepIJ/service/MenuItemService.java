package com.example.DoantotnghiepIJ.service;

import com.example.DoantotnghiepIJ.dto.Menu.MenuItemDto;
import com.example.DoantotnghiepIJ.entity.Category;
import com.example.DoantotnghiepIJ.entity.MenuItem;
import com.example.DoantotnghiepIJ.entity.MenuItemImage;
import com.example.DoantotnghiepIJ.repository.CategoryRepository;
import com.example.DoantotnghiepIJ.repository.MenuItemImageRepository;
import com.example.DoantotnghiepIJ.repository.MenuItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemImageRepository menuItemImageRepository;
    private final CloudinaryService cloudinaryService;

    // ================== CREATE ==================
    public MenuItem create(MenuItemDto dto) {

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        String slug = generateSlug(dto.getSlug(), dto.getName());

        MenuItem item = MenuItem.builder()
                .name(dto.getName())
                .slug(slug)
                .description(dto.getDescription())
                .price(dto.getPrice())
                .discountPrice(dto.getDiscountPrice())
                .category(category)
                .isActive(true)
                .build();

        return menuItemRepository.save(item);
    }

    // ================== GET ALL (PAGING + SORT) ==================
    public Page<MenuItem> getAll(int page, int size, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return menuItemRepository.findByIsDeletedFalse(pageable);
    }

    // ================== GET BY ID ==================
    public MenuItem getById(UUID id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
    }

    // ================== UPDATE ==================
    public MenuItem update(UUID id, MenuItemDto dto) {

        MenuItem item = getById(id);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // check slug
        if (!item.getSlug().equals(dto.getSlug())) {
            String newSlug = generateSlug(dto.getSlug(), dto.getName());
            item.setSlug(newSlug);
        }

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setDiscountPrice(dto.getDiscountPrice());
        item.setCategory(category);
        item.setIsActive(dto.getIsActive());

        return menuItemRepository.save(item);
    }

    // ================== DELETE (SOFT) ==================
    public void delete(UUID id) {
        MenuItem item = getById(id);
        item.setIsDeleted(true);
        menuItemRepository.save(item);
    }

    // ================== TOGGLE STATUS ==================
    public void toggleStatus(UUID id) {
        MenuItem item = getById(id);
        item.setIsActive(!item.getIsActive());
        menuItemRepository.save(item);
    }

    // ================== SEARCH + FILTER ==================
    public Page<MenuItem> search(
            String keyword,
            Boolean isActive,
            UUID categoryId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        // CASE 1: full filter
        if (keyword != null && isActive != null && categoryId != null) {
            return menuItemRepository
                    .findByNameContainingIgnoreCaseAndIsActiveAndCategory_IdAndIsDeletedFalse(
                            keyword, isActive, categoryId, pageable);
        }

        // CASE 2: keyword + status
        if (keyword != null && isActive != null) {
            return menuItemRepository
                    .findByNameContainingIgnoreCaseAndIsActiveAndIsDeletedFalse(
                            keyword, isActive, pageable);
        }

        // CASE 3: keyword
        if (keyword != null) {
            return menuItemRepository
                    .findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword, pageable);
        }

        // DEFAULT
        return menuItemRepository.findByIsDeletedFalse(pageable);
    }

    // ================== UPLOAD IMAGE ==================
    @Transactional
    public void uploadImages(UUID menuItemId,
                             MultipartFile thumbnail,
                             List<MultipartFile> images) {

        MenuItem item = getById(menuItemId);

        // thumbnail
        if (thumbnail != null && !thumbnail.isEmpty()) {
            Map result = cloudinaryService.upload(thumbnail);
            item.setThumbnail(result.get("secure_url").toString());
        }

        // images
        if (images != null) {
            for (MultipartFile file : images) {

                Map result = cloudinaryService.upload(file);

                MenuItemImage img = MenuItemImage.builder()
                        .imageUrl(result.get("secure_url").toString())
                        .publicId(result.get("public_id").toString())
                        .menuItem(item)
                        .build();

                menuItemImageRepository.save(img);
            }
        }

        menuItemRepository.save(item);
    }

    // ================== SLUG ==================
    private String generateSlug(String slug, String name) {

        String base = (slug != null && !slug.isBlank())
                ? slug
                : name.toLowerCase()
                .replace(" ", "-")
                .replaceAll("[^a-z0-9-]", "");

        String finalSlug = base;
        int count = 1;

        while (menuItemRepository.existsBySlug(finalSlug)) {
            finalSlug = base + "-" + count++;
        }

        return finalSlug;
    }
//    dashboard stats
    public Map<String, Long> getDashboardStats() {

        long total = menuItemRepository.countByIsDeletedFalse();

        long active = menuItemRepository.countByIsDeletedFalseAndIsActiveTrue();

        Map<String, Long> result = new HashMap<>();
        result.put("total", total);
        result.put("active", active);

        return result;
    }
}