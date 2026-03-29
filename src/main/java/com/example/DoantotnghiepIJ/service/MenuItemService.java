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

        int quantity = dto.getQuantity() != null ? dto.getQuantity() : 0;

        MenuItem item = MenuItem.builder()
                .name(dto.getName())
                .slug(slug)
                .description(dto.getDescription())
                .price(dto.getPrice())
                .discountPrice(dto.getDiscountPrice())
                .category(category)
                .quantity(quantity)
                .isAvailable(quantity > 0)
                .isActive(true)
                .build();

        return menuItemRepository.save(item);
    }

    // ================== GET ALL ==================
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

        // slug
        if (dto.getSlug() != null && !dto.getSlug().equals(item.getSlug())) {
            item.setSlug(generateSlug(dto.getSlug(), dto.getName()));
        }

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setDiscountPrice(dto.getDiscountPrice());
        item.setCategory(category);

        // update status
        if (dto.getIsActive() != null) {
            item.setIsActive(dto.getIsActive());
        }

        // update quantity
        if (dto.getQuantity() != null) {
            item.setQuantity(dto.getQuantity());
            item.setIsAvailable(dto.getQuantity() > 0);
        }

        return menuItemRepository.save(item);
    }

    // ================== DELETE ==================
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

    // ================== INCREASE STOCK ==================
    public void increaseStock(UUID id, int amount) {

        if (amount <= 0) throw new RuntimeException("Amount phải > 0");

        MenuItem item = getById(id);

        item.setQuantity(item.getQuantity() + amount);
        item.setIsAvailable(true);

        menuItemRepository.save(item);
    }

    // ================== DECREASE STOCK ==================
    public void decreaseStock(UUID id, int amount) {

        if (amount <= 0) throw new RuntimeException("Amount phải > 0");

        MenuItem item = getById(id);

        if (item.getQuantity() < amount) {
            throw new RuntimeException("Không đủ hàng");
        }

        item.setQuantity(item.getQuantity() - amount);

        if (item.getQuantity() <= 0) {
            item.setIsAvailable(false);
        }

        menuItemRepository.save(item);
    }

    // ================== SEARCH ==================
    public Page<MenuItem> search(
            String keyword,
            Boolean isActive,
            UUID categoryId,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (keyword == null || keyword.isBlank()) {
            return menuItemRepository.findByIsDeletedFalse(pageable);
        }

        if (isActive != null && categoryId != null) {
            return menuItemRepository
                    .findByNameContainingIgnoreCaseAndIsActiveAndCategory_IdAndIsDeletedFalse(
                            keyword, isActive, categoryId, pageable);
        }

        if (isActive != null) {
            return menuItemRepository
                    .findByNameContainingIgnoreCaseAndIsActiveAndIsDeletedFalse(
                            keyword, isActive, pageable);
        }

        return menuItemRepository
                .findByNameContainingIgnoreCaseAndIsDeletedFalse(keyword, pageable);
    }

    // ================== UPLOAD IMAGE ==================
    @Transactional
    public void uploadImages(UUID menuItemId,
                             MultipartFile thumbnail,
                             List<MultipartFile> images) {

        MenuItem item = getById(menuItemId);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            Map result = cloudinaryService.upload(thumbnail);
            item.setThumbnail(result.get("secure_url").toString());
        }

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

    // ================== DASHBOARD ==================
    public Map<String, Long> getDashboardStats() {

        long total = menuItemRepository.countByIsDeletedFalse();
        long active = menuItemRepository.countByIsDeletedFalseAndIsActiveTrue();

        Map<String, Long> result = new HashMap<>();
        result.put("total", total);
        result.put("active", active);

        return result;
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
}