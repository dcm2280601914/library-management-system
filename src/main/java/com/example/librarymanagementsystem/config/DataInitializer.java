package com.example.librarymanagementsystem.config;

import com.example.librarymanagementsystem.entity.Employee;
import com.example.librarymanagementsystem.enums.Role;
import com.example.librarymanagementsystem.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (employeeRepository.findByUsername("admin").isEmpty()) {
                Employee admin = new Employee();
                admin.setFullName("Administrator");
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode("123456"));
                admin.setEmail("admin@gmail.com");
                admin.setPhone("0123456789");
                admin.setRole(Role.ADMIN);
                admin.setActive(true);

                employeeRepository.save(admin);
            }

            if (employeeRepository.findByUsername("staff").isEmpty()) {
                Employee staff = new Employee();
                staff.setFullName("Nhân viên");
                staff.setUsername("staff");
                staff.setPasswordHash(passwordEncoder.encode("123456"));
                staff.setEmail("staff@gmail.com");
                staff.setPhone("0987654321");
                staff.setRole(Role.STAFF);
                staff.setActive(true);

                employeeRepository.save(staff);
            }
        };
    }
}