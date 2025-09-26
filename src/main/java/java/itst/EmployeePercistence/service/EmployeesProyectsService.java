package java.itst.EmployeePercistence.service;

import java.itst.EmployeePercistence.dto.EmployeesProyectsRequest;
import java.itst.EmployeePercistence.dto.EmployeesProyectsResponse;
import java.util.List;

public class EmployeesProyectsService {
    List<EmployeesProyectsResponse> findAll();
    EmployeesProyectsResponse findById(Integer id);
    EmployeesProyectsResponse create(EmployeesProyectsRequest request);
    void delete(Integer id);
    
}
