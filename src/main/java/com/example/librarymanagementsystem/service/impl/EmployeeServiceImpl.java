package com.example.libmanagement.service.impl;

import com.example.libmanagement.entity.Employee;
import com.example.libmanagement.repository.EmployeeRepository;
import com.example.libmanagement.service.EmployeeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    @Override
    public Employee findById(Long id) {
        return employeeRepository.findById(id).orElse(null);
    }

    @Override
    public Employee save(Employee employee) {
        return employeeRepository.save(employee);
    }

    @Override
    public Employee update(Long id, Employee employee) {
        Employee existingEmployee = findById(id);
        if (existingEmployee == null) {
            return null;
        }

        existingEmployee.setFullName(employee.getFullName());
        existingEmployee.setUsername(employee.getUsername());
        existingEmployee.setPasswordHash(employee.getPasswordHash());
        existingEmployee.setEmail(employee.getEmail());
        existingEmployee.setPhone(employee.getPhone());
        existingEmployee.setRole(employee.getRole());
        existingEmployee.setActive(employee.getActive());

        return employeeRepository.save(existingEmployee);
    }

    @Override
    public void delete(Long id) {
        employeeRepository.deleteById(id);
    }

    @Override
    public Optional<Employee> findByUsername(String username) {
        return employeeRepository.findByUsername(username);
    }

    @Override
    public void resetPassword(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên."));

        employee.setPasswordHash(passwordEncoder.encode("123456"));
        employeeRepository.save(employee);
    }
}