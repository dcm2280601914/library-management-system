package com.example.libmanagement.service;

import com.example.libmanagement.dto.CategoryStatisticsDto;
import com.example.libmanagement.entity.Category;
import com.example.libmanagement.enums.CategoryStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CategoryService {

    List<Category> findAll();

    Page<CategoryStatisticsDto> getCategoryStatistics(String keyword,
                                                      CategoryStatus status,
                                                      int page,
                                                      int size,
                                                      String sortField,
                                                      String sortDir);

    CategoryStatisticsDto getCategoryStatisticsById(Long id);

    Category findById(Long id);

    Category save(Category category);

    Category update(Long id, Category category);

    void delete(Long id);

    void toggleStatus(Long id);
}