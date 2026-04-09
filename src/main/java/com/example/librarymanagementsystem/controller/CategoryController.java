package com.example.librarymanagementsystem.controller;


import com.example.librarymanagementsystem.dto.CategoryStatisticsDto;
import com.example.librarymanagementsystem.entity.Category;
import com.example.librarymanagementsystem.enums.CategoryStatus;
import com.example.librarymanagementsystem.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ModelAttribute("categoryStatuses")
    public CategoryStatus[] categoryStatuses() {
        return CategoryStatus.values();
    }

    @GetMapping
    public String listCategories(@RequestParam(defaultValue = "") String keyword,
                                 @RequestParam(required = false) CategoryStatus status,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(defaultValue = "id") String sortField,
                                 @RequestParam(defaultValue = "asc") String sortDir,
                                 Model model) {

        Page<CategoryStatisticsDto> categoryPage = categoryService.getCategoryStatistics(
                keyword, status, page, size, sortField, sortDir
        );

        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("categories", categoryPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("size", size);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equalsIgnoreCase("asc") ? "desc" : "asc");

        return "categories/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        Category category = new Category();
        category.setStatus(CategoryStatus.VISIBLE);
        model.addAttribute("category", category);
        return "categories/add";
    }

    @PostMapping("/save")
    public String saveCategory(@ModelAttribute("category") Category category,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            categoryService.save(category);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm thể loại thành công.");
            return "redirect:/categories";
        } catch (IllegalArgumentException e) {
            model.addAttribute("category", category);
            model.addAttribute("errorMessage", e.getMessage());
            return "categories/add";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        return "categories/edit";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @ModelAttribute("category") Category category,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            categoryService.update(id, category);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thể loại thành công.");
            return "redirect:/categories";
        } catch (IllegalArgumentException e) {
            category.setId(id);
            model.addAttribute("category", category);
            model.addAttribute("errorMessage", e.getMessage());
            return "categories/edit";
        }
    }

    @GetMapping("/detail/{id}")
    public String viewDetail(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.getCategoryStatisticsById(id));
        return "categories/detail";
    }

    @GetMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.toggleStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái thể loại.");
        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thể loại thành công.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa thể loại này.");
        }
        return "redirect:/categories";
    }
}