package com.example.librarymanagementsystem.service;

import com.example.librarymanagementsystem.entity.BorrowRecord;

import java.util.List;
import java.util.Optional;

public interface BorrowRecordService {
    List<BorrowRecord> findAll();
    BorrowRecord findById(Long id);
    BorrowRecord save(BorrowRecord borrowRecord);
    BorrowRecord update(Long id, BorrowRecord borrowRecord);
    void delete(Long id);
    Optional<BorrowRecord> findByBorrowCode(String borrowCode);
    List<BorrowRecord> searchByBorrowerName(String keyword);
}