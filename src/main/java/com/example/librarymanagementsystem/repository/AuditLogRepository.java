package com.example.libmanagement.repository;

import com.example.libmanagement.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findAllByOrderByCreatedAtDesc();

    List<AuditLog> findByUsernameContainingIgnoreCaseOrderByCreatedAtDesc(String username);

    List<AuditLog> findByModuleNameContainingIgnoreCaseOrderByCreatedAtDesc(String moduleName);
}