package com.example.libmanagement.repository;

import com.example.libmanagement.entity.ReturnRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReturnRecordRepository extends JpaRepository<ReturnRecord, Long> {

    Optional<ReturnRecord> findByBorrowRecordId(Long borrowRecordId);

    boolean existsByBorrowRecordId(Long borrowRecordId);

    Optional<ReturnRecord> findByReturnCode(String returnCode);

    List<ReturnRecord> findByBorrowRecordBorrowerFullNameContainingIgnoreCase(String keyword);
}