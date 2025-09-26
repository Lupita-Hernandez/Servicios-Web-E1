package java.itst.EmployeePercistence.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import java.itst.EmployeePercistence.model.Employee;

@Repository
public class EmployeeRepository {
    private final Map<UUID, Employee> data = new ConcurrentHashMap<>();

    public Employee add(Employee employee) {
        if (employee.getId() == null)
            employee.setId(UUID.randomUUID());
        data.put(employee.getId(), employee);
        return employee;
    }

    public Employee getById(int id) {
        return data.values().stream()
                .filter(emp -> emp.getId().hashCode() == id)
                .findFirst()
                .orElse(null);
    }   

    public boolean deleteById(UUID id) {
        return data.remove(id) != null;
    }

    public Employee updateById(int id, Employee updatedEmployee) {
        Employee existingEmployee = getById(id);
            updatedEmployee.setId(existingEmployee.getId());
            data.put(existingEmployee.getId(), updatedEmployee);
            return updatedEmployee;
        
    }

    public java.util.List<Employee> getAll() {
        return data.values().stream().toList();
    }

     public List<Employee> findAll() {
        return new ArrayList<>(data.values());
    }

    public Optional<Employee> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }
}
