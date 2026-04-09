package com.example.libmanagement.service;

import com.example.libmanagement.entity.BorrowRecord;

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