package com.example.libmanagement.service;

import com.example.libmanagement.entity.AuditLog;

import java.util.List;

public interface AuditLogService {

    void log(String action, String moduleName, String entityName, Long entityId, String description);

    List<AuditLog> findAll();

    List<AuditLog> searchByUsername(String username);

    List<AuditLog> searchByModuleName(String moduleName);
}