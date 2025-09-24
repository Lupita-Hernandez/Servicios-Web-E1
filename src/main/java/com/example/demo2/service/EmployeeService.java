package com.example.demo2.service;

import java.util.List;
import java.util.UUID;

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

    public EmployeeResponse getById(int id) {
        Employee employee = repo.getById(id);
        return toResponse(employee);
    }

   public void deleteById(UUID id) {
        if (!repo.deleteById(id)) {
            throw new IllegalArgumentException("Estudiante no encontrado: " + id);
        }
    }

    public EmployeeResponse updateById(int id, EmployeeRequest employeeRequest) {
        Employee existingEmployee = repo.getById(id);
            existingEmployee.setName(employeeRequest.name());
            existingEmployee.setLastname(employeeRequest.lastname());
            existingEmployee.setDepartment(employeeRequest.department());
            repo.add(existingEmployee); // Re-add to update
            return toResponse(existingEmployee);

    }

    //update
    public EmployeeResponse update(UUID id, EmployeeRequest employeeRequest) {
        Employee existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        existing.setName(employeeRequest.name());
        existing.setLastname(employeeRequest.lastname());
        existing.setDepartment(employeeRequest.department());

        repo.add(existing); // vuelve a guardar
        return toResponse(existing);
    }

    public List<EmployeeResponse> getAll() {
        return repo.getAll().stream()
                .map(this::toResponse)
                .toList();
    }


    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getName(),
                employee.getLastname(),
                employee.getDepartment()
        );
    }

    public List<EmployeeResponse> listar() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }


}

