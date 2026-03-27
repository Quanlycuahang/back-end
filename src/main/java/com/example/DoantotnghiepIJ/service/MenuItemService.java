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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;
    private final MenuItemImageRepository menuItemImageRepository;
    //  CREATE
    public MenuItem create(MenuItemDto dto) {

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (menuItemRepository.existsBySlug(dto.getSlug())) {
            throw new RuntimeException("Slug already exists");
        }

        if (dto.getDiscountPrice() != null &&
                dto.getDiscountPrice() > dto.getPrice()) {
            throw new RuntimeException("Discount must <= price");
        }

        MenuItem item = MenuItem.builder()
                .name(dto.getName())
                .slug(generateSlug(dto.getSlug(), dto.getName()))
                .description(dto.getDescription())
                .price(dto.getPrice())
                .discountPrice(dto.getDiscountPrice())
                .category(category)
                .build();

        return menuItemRepository.save(item);
    }

    //  GET ALL
    public List<MenuItem> getAll() {
        return menuItemRepository.findByIsDeletedFalse();
    }

    //  GET BY ID
    public MenuItem getById(UUID id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
    }

    //  UPDATE
    public MenuItem update(UUID id, MenuItemDto dto) {

        MenuItem item = getById(id);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!item.getSlug().equals(dto.getSlug()) &&
                menuItemRepository.existsBySlug(dto.getSlug())) {
            throw new RuntimeException("Slug already exists");
        }

        item.setName(dto.getName());
        item.setSlug(generateSlug(dto.getSlug(), dto.getName()));
        item.setDescription(dto.getDescription());
        item.setPrice(dto.getPrice());
        item.setDiscountPrice(dto.getDiscountPrice());
        item.setCategory(category);

        return menuItemRepository.save(item);
    }

    //  DELETE (SOFT)
    public void delete(UUID id) {
        MenuItem item = getById(id);
        item.setIsDeleted(true);
        menuItemRepository.save(item);
    }

    //  helper slug
    private String generateSlug(String slug, String name) {
        if (slug != null && !slug.isBlank()) return slug;

        return name.toLowerCase()
                .replace(" ", "-")
                .replaceAll("[^a-z0-9-]", "");
    }


//    upload ảnh
@Transactional
public void uploadImages(UUID menuItemId,
                         MultipartFile thumbnail,
                         List<MultipartFile> images) {

    MenuItem item = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new RuntimeException("Not found"));

    // thumbnail
    if (thumbnail != null && !thumbnail.isEmpty()) {

        Map result = cloudinaryService.upload(thumbnail);

        item.setThumbnail(result.get("secure_url").toString());
    }

    // ảnh phụ
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
}