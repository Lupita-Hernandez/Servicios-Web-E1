package java.itst.EmployeePercistence.service;

import java.itst.EmployeePercistence.dto.EmployeesProyectsRequest;
import java.itst.EmployeePercistence.dto.EmployeesProyectsResponse;
import java.itst.EmployeePercistence.model.Employee;
import java.itst.EmployeePercistence.model.EmployeesProyects;
import java.itst.EmployeePercistence.model.Proyect;
import java.itst.EmployeePercistence.repository.EmployeesProyectsRepository;
import java.itst.EmployeePercistence.mapper.EmployeesProyectsMapper;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
@Service
//@RequiredArgsConstructor
public class EmployeesProyectsServiceImpl {
    private final EmployeesProyectsRepository employeesProyectsRepository;
    private final EmployeeRepository employeeRepository;
    private final ProyectRepository proyectRepository;

    public List<EmployeesProyectsResponse> findAll() {
        return employeesProyectsRepository.findAll().stream()
                .map(EmployeesProyectsMapper::toResponse)
                .toList();
    }

    public EmployeesProyectsResponse findById(Integer id) {
        EmployeesProyects entity = employeesProyectsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("EmployeesProyects not found: " + id));
        return EmployeesProyectsMapper.toResponse(entity);
    }

    public EmployeesProyectsResponse create(EmployeesProyectsRequest request) {
        // Buscar entidades relacionadas
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new EntityNotFoundException("Empleado no encontrado: " + request.getEmployeeId()));
        Proyect proyect = proyectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado: " + request.getProjectId()));

        // Construir entidad
        EmployeesProyects entity = EmployeesProyects.builder()
                .employee(employee)
                .proyect(proyect)
                .build();

        EmployeesProyects saved = employeesProyectsRepository.save(entity);

        return EmployeesProyectsMapper.toResponse(saved);
    }

    
    public void delete(Integer id) {
        if (!employeesProyectsRepository.existsById(id)) {
            throw new EntityNotFoundException("EmployeesProyects not found: " + id);
        }
        employeesProyectsRepository.deleteById(id);
    }
}
