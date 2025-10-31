package com.stackoverflow.service.common;

import com.stackoverflow.entity.Category;
import com.stackoverflow.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Category Service - Quản lý chuyên mục
 */
@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Tạo chuyên mục mới
     */
    public Category createCategory(Category category) {
        category.setCreatedAt(LocalDateTime.now());
        category.setQuestionCount(0);
        
        return categoryRepository.save(category);
    }

    /**
     * Lấy tất cả chuyên mục
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Lấy chuyên mục với phân trang
     */
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    /**
     * Lấy chuyên mục theo ID
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Lấy chuyên mục theo slug
     */
    public Optional<Category> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    /**
     * Lấy chuyên mục theo tên
     */
    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    /**
     * Tìm kiếm chuyên mục
     */
    public List<Category> searchCategories(String keyword) {
        return categoryRepository.findByNameContainingOrDescriptionContaining(keyword, keyword);
    }

    /**
     * Cập nhật chuyên mục
     */
    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }

    /**
     * Xóa chuyên mục
     */
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    /**
     * Thay đổi thứ tự hiển thị
     */
    public void updateDisplayOrder(Long id, Integer newOrder) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.setDisplayOrder(newOrder);
        categoryRepository.save(category);
    }

    /**
     * Lấy danh sách chuyên mục theo thứ tự hiển thị
     */
    public List<Category> getCategoriesOrderedByDisplayOrder() {
        return categoryRepository.findAllByOrderByDisplayOrderAsc();
    }

    /**
     * Đếm số lượng chuyên mục
     */
    public long countCategories() {
        return categoryRepository.count();
    }

    /**
     * Tăng số lượng câu hỏi trong chuyên mục
     */
    public void incrementQuestionCount(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        category.setQuestionCount(category.getQuestionCount() + 1);
        categoryRepository.save(category);
    }

    /**
     * Giảm số lượng câu hỏi trong chuyên mục
     */
    public void decrementQuestionCount(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (category.getQuestionCount() > 0) {
            category.setQuestionCount(category.getQuestionCount() - 1);
            categoryRepository.save(category);
        }
    }

    /**
     * Kiểm tra tên chuyên mục đã tồn tại chưa
     */
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    /**
     * Kiểm tra slug đã tồn tại chưa
     */
    public boolean existsBySlug(String slug) {
        return categoryRepository.existsBySlug(slug);
    }

    /**
     * Tạo slug từ tên
     */
    public String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-+", "-");
    }

    /**
     * Sắp xếp lại thứ tự các chuyên mục
     */
    public void reorderCategories(List<Long> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));
            
            category.setDisplayOrder(i + 1);
            categoryRepository.save(category);
        }
    }
}

