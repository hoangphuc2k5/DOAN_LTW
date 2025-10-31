package com.stackoverflow.repository;

import com.stackoverflow.entity.Category;
import com.stackoverflow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByParentIsNullOrderByDisplayOrderAsc();
    
    List<Category> findByParentOrderByDisplayOrderAsc(Category parent);
    
    Optional<Category> findByName(String name);
    
    List<Category> findByManager(User manager);
    
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    Boolean existsByName(String name);
    
    // Additional methods for admin management
    Optional<Category> findBySlug(String slug);
    
    List<Category> findByNameContainingOrDescriptionContaining(String name, String description);
    
    List<Category> findAllByOrderByDisplayOrderAsc();
    
    boolean existsBySlug(String slug);
}

