package com.example.demo2.service;

import org.springframework.stereotype.Service;

import com.example.demo2.dto.EmployeeRequest;
import com.example.demo2.dto.EmployeeResponse;
import com.example.demo2.model.Employee;
import com.example.demo2.repository.EmployeeRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = repo;
    }

    public EmployeeResponse add(EmployeeRequest employeeRequest) {
        Employee employee = Employee.builder()
                .name(employeeRequest.name())
                .lastname(employeeRequest.lastname())
                .department(employeeRequest.department())
                .build();

        repo.add(employee);
        return toResponse(employee);
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getLastname(),
                employee.getDepartment()
        );
    }
}

