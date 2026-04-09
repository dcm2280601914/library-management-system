package com.example.libmanagement.service;

import com.example.libmanagement.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {
    List<Employee> findAll();
    Employee findById(Long id);
    Employee save(Employee employee);
    Employee update(Long id, Employee employee);
    void delete(Long id);
    Optional<Employee> findByUsername(String username);

    void resetPassword(Long id);
}