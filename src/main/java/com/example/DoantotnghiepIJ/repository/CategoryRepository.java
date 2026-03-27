package com.example.DoantotnghiepIJ.repository;

import com.example.DoantotnghiepIJ.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    List<Category> findByParentIsNull();
    List<Category> findByDeletedFalse();
    List<Category> findByParentIsNullAndDeletedFalse();
}
