package com.example.DoantotnghiepIJ.repository;

import com.example.DoantotnghiepIJ.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    // check slug
    boolean existsBySlug(String slug);

    // get all (paging)
    Page<MenuItem> findByIsDeletedFalse(Pageable pageable);

    // search theo name
    Page<MenuItem> findByNameContainingIgnoreCaseAndIsDeletedFalse(
            String name, Pageable pageable);

    // search + status
    Page<MenuItem> findByNameContainingIgnoreCaseAndIsActiveAndIsDeletedFalse(
            String name, Boolean isActive, Pageable pageable);

    // search + status + category
    Page<MenuItem> findByNameContainingIgnoreCaseAndIsActiveAndCategory_IdAndIsDeletedFalse(
            String name, Boolean isActive, UUID categoryId, Pageable pageable);
    long countByIsDeletedFalse();

    long countByIsDeletedFalseAndIsActiveTrue();
}