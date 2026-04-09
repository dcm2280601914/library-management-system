package com.example.libmanagement.service;

import com.example.libmanagement.entity.ReturnRecord;

import java.util.List;
import java.util.Optional;

public interface ReturnRecordService {
    List<ReturnRecord> findAll();
    ReturnRecord findById(Long id);
    ReturnRecord save(ReturnRecord returnRecord);
    ReturnRecord update(Long id, ReturnRecord returnRecord);
    void delete(Long id);
    Optional<ReturnRecord> findByReturnCode(String returnCode);
    List<ReturnRecord> searchByBorrowerName(String keyword);
}