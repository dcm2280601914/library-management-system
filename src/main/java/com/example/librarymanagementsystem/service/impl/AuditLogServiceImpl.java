package com.example.libmanagement.service.impl;

import com.example.libmanagement.entity.AuditLog;
import com.example.libmanagement.entity.Employee;
import com.example.libmanagement.repository.AuditLogRepository;
import com.example.libmanagement.repository.EmployeeRepository;
import com.example.libmanagement.service.AuditLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final EmployeeRepository employeeRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository,
                               EmployeeRepository employeeRepository) {
        this.auditLogRepository = auditLogRepository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void log(String action, String moduleName, String entityName, Long entityId, String description) {
        AuditLog auditLog = new AuditLog();

        String username = "SYSTEM";
        String fullName = "System";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {

            username = authentication.getName();

            Employee employee = employeeRepository.findByUsername(username).orElse(null);
            if (employee != null) {
                fullName = employee.getFullName();
            } else {
                fullName = username;
            }
        }

        auditLog.setUsername(username);
        auditLog.setFullName(fullName);
        auditLog.setAction(action);
        auditLog.setModuleName(moduleName);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);

        auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public List<AuditLog> searchByUsername(String username) {
        return auditLogRepository.findByUsernameContainingIgnoreCaseOrderByCreatedAtDesc(username);
    }

    @Override
    public List<AuditLog> searchByModuleName(String moduleName) {
        return auditLogRepository.findByModuleNameContainingIgnoreCaseOrderByCreatedAtDesc(moduleName);
    }
}