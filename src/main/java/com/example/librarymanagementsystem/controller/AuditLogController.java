package com.example.librarymanagementsystem.controller;

import com.example.librarymanagementsystem.entity.AuditLog;
import com.example.librarymanagementsystem.service.AuditLogService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public String listAuditLogs(@RequestParam(required = false) String username,
                                @RequestParam(required = false) String module,
                                Model model) {

        List<AuditLog> auditLogs;

        boolean hasUsername = username != null && !username.trim().isEmpty();
        boolean hasModule = module != null && !module.trim().isEmpty();

        if (hasUsername) {
            auditLogs = auditLogService.searchByUsername(username.trim());
        } else if (hasModule) {
            auditLogs = auditLogService.searchByModuleName(module.trim());
        } else {
            auditLogs = auditLogService.findAll();
        }

        model.addAttribute("auditLogs", auditLogs);
        model.addAttribute("username", username);
        model.addAttribute("module", module);

        return "audit-logs/list";
    }
}