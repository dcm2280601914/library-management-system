package com.example.librarymanagementsystem.service;

import com.example.librarymanagementsystem.dto.CategoryStatisticsDto;
import com.example.librarymanagementsystem.entity.Category;
import com.example.librarymanagementsystem.enums.CategoryStatus;
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