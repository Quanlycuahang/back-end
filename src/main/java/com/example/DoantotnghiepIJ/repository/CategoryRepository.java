package com.example.DoantotnghiepIJ.repository;

import com.example.DoantotnghiepIJ.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByParentIsNull();
    List<Category> findByDeletedFalse();
    List<Category> findByParentIsNullAndDeletedFalse();

    Page<Category> findByDeletedFalse(Pageable pageable);

    Page<Category> findByDeletedFalseAndNameContainingIgnoreCase(
            String name, Pageable pageable
    );
    @Query("""
    SELECT COUNT(c) 
    FROM Category c 
    WHERE c.deleted = false
""")
    long countAllCategories();
    @Query("""
    SELECT COUNT(c) 
    FROM Category c 
    LEFT JOIN MenuItem m ON m.category.id = c.id
    WHERE c.deleted = false AND m.id IS NULL
""")
    long countEmptyCategories();
}
