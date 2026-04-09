package com.example.libmanagement.dto;

import com.example.libmanagement.enums.CategoryStatus;

public class CategoryStatisticsDto {

    private Long id;
    private String name;
    private String description;
    private CategoryStatus status;
    private long bookCount;
    private long borrowCount;

    public CategoryStatisticsDto(Long id, String name, String description,
                                 CategoryStatus status, long bookCount, long borrowCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.bookCount = bookCount;
        this.borrowCount = borrowCount;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public CategoryStatus getStatus() {
        return status;
    }

    public long getBookCount() {
        return bookCount;
    }

    public long getBorrowCount() {
        return borrowCount;
    }
}