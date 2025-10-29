package com.stackoverflow.controller.admin;

import com.stackoverflow.entity.Category;
import com.stackoverflow.service.common.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Admin Category Controller - Quản lý chuyên mục
 */
@Controller
@RequestMapping("/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * Danh sách chuyên mục
     */
    @GetMapping
    public String listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("displayOrder").ascending());
        Page<Category> categories = categoryService.getAllCategories(pageable);
        
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", categories.getTotalPages());
        model.addAttribute("pageTitle", "Category Management - Admin");
        
        return "admin/categories/list";
    }

    /**
     * Form tạo chuyên mục mới
     */
    @GetMapping("/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Create New Category - Admin");
        
        return "admin/categories/form";
    }

    /**
     * Xử lý tạo chuyên mục mới
     */
    @PostMapping("/new")
    public String createCategory(
            @Valid @ModelAttribute Category category,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        // Validate
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Create New Category - Admin");
            return "admin/categories/form";
        }
        
        // Check if name already exists
        if (categoryService.existsByName(category.getName())) {
            model.addAttribute("errorMessage", "Category name already exists!");
            model.addAttribute("pageTitle", "Create New Category - Admin");
            return "admin/categories/form";
        }
        
        // Generate slug if empty
        if (category.getSlug() == null || category.getSlug().isEmpty()) {
            category.setSlug(categoryService.generateSlug(category.getName()));
        }
        
        // Check if slug already exists
        if (categoryService.existsBySlug(category.getSlug())) {
            model.addAttribute("errorMessage", "Category slug already exists!");
            model.addAttribute("pageTitle", "Create New Category - Admin");
            return "admin/categories/form";
        }
        
        try {
            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Category created successfully!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            model.addAttribute("pageTitle", "Create New Category - Admin");
            return "admin/categories/form";
        }
    }

    /**
     * Form chỉnh sửa chuyên mục
     */
    @GetMapping("/{id}/edit")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        model.addAttribute("category", category);
        model.addAttribute("pageTitle", "Edit Category - " + category.getName());
        
        return "admin/categories/form";
    }

    /**
     * Xử lý cập nhật chuyên mục
     */
    @PostMapping("/{id}/edit")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute Category category,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Category");
            return "admin/categories/form";
        }
        
        try {
            Category existingCategory = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            // Update fields
            existingCategory.setName(category.getName());
            existingCategory.setDescription(category.getDescription());
            existingCategory.setSlug(category.getSlug());
            existingCategory.setIcon(category.getIcon());
            existingCategory.setColor(category.getColor());
            existingCategory.setDisplayOrder(category.getDisplayOrder());
            existingCategory.setIsActive(category.getIsActive());
            
            categoryService.updateCategory(existingCategory);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Category updated successfully!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            model.addAttribute("pageTitle", "Edit Category");
            return "admin/categories/form";
        }
    }

    /**
     * Xóa chuyên mục
     */
    @PostMapping("/{id}/delete")
    public String deleteCategory(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Category deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }

    /**
     * Sắp xếp lại thứ tự
     */
    @PostMapping("/reorder")
    @ResponseBody
    public String reorderCategories(@RequestBody List<Long> categoryIds) {
        try {
            categoryService.reorderCategories(categoryIds);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    /**
     * Thay đổi trạng thái active
     */
    @PostMapping("/{id}/toggle-active")
    public String toggleActive(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        try {
            Category category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            category.setIsActive(!category.getIsActive());
            categoryService.updateCategory(category);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Category status updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/admin/categories";
    }
}

