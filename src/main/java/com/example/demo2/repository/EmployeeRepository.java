package com.example.demo2.repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.example.demo2.model.Employee;

@Repository
public class EmployeeRepository {
    
    private final Map<UUID, Employee> data = new ConcurrentHashMap<>();

    public Employee add(Employee employee) {
        if (employee.getId() == null)
            employee.setId(UUID.randomUUID());
        data.put(employee.getId(), employee);
        return employee;
    }
}
