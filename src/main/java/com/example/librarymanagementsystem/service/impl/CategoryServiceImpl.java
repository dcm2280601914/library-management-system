package com.example.libmanagement.service.impl;

import com.example.libmanagement.dto.CategoryStatisticsDto;
import com.example.libmanagement.entity.Category;
import com.example.libmanagement.enums.CategoryStatus;
import com.example.libmanagement.repository.CategoryRepository;
import com.example.libmanagement.service.CategoryService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<CategoryStatisticsDto> getCategoryStatistics(String keyword,
                                                             CategoryStatus status,
                                                             int page,
                                                             int size,
                                                             String sortField,
                                                             String sortDir) {

        String safeSortField = resolveSortField(sortField);
        Sort sort = "desc".equalsIgnoreCase(sortDir)
                ? Sort.by(safeSortField).descending()
                : Sort.by(safeSortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return categoryRepository.findCategoryStatistics(keyword, status, pageable);
    }

    @Override
    public CategoryStatisticsDto getCategoryStatisticsById(Long id) {
        return categoryRepository.findCategoryStatisticsById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thống kê thể loại với ID: " + id));
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thể loại với ID: " + id));
    }

    @Override
    public Category save(Category category) {
        normalize(category);

        if (!StringUtils.hasText(category.getName())) {
            throw new IllegalArgumentException("Tên thể loại không được để trống.");
        }

        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Tên thể loại đã tồn tại.");
        }

        if (category.getStatus() == null) {
            category.setStatus(CategoryStatus.VISIBLE);
        }

        return categoryRepository.save(category);
    }

    @Override
    public Category update(Long id, Category category) {
        Category existing = findById(id);

        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        existing.setStatus(category.getStatus());

        normalize(existing);

        if (!StringUtils.hasText(existing.getName())) {
            throw new IllegalArgumentException("Tên thể loại không được để trống.");
        }

        if (categoryRepository.existsByNameAndIdNot(existing.getName(), id)) {
            throw new IllegalArgumentException("Tên thể loại đã tồn tại.");
        }

        if (existing.getStatus() == null) {
            existing.setStatus(CategoryStatus.VISIBLE);
        }

        return categoryRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }

    @Override
    public void toggleStatus(Long id) {
        Category category = findById(id);
        if (category.getStatus() == CategoryStatus.VISIBLE) {
            category.setStatus(CategoryStatus.HIDDEN);
        } else {
            category.setStatus(CategoryStatus.VISIBLE);
        }
        categoryRepository.save(category);
    }

    private void normalize(Category category) {
        if (category.getName() != null) {
            category.setName(category.getName().trim());
        }
        if (category.getDescription() != null) {
            category.setDescription(category.getDescription().trim());
            if (category.getDescription().isEmpty()) {
                category.setDescription(null);
            }
        }
    }

    private String resolveSortField(String sortField) {
        Set<String> allowed = Set.of("id", "name", "status");
        return allowed.contains(sortField) ? sortField : "id";
    }
}