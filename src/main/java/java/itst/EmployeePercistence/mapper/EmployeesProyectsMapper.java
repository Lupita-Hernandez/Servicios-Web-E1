package java.itst.EmployeePercistence.mapper;

import java.itst.EmployeePercistence.dto.EmployeesProyectsRequest;
import java.itst.EmployeePercistence.dto.EmployeesProyectsResponse;
import java.itst.EmployeePercistence.model.Employee;
import java.itst.EmployeePercistence.model.EmployeesProyects;
import java.itst.EmployeePercistence.model.Proyect;

public class EmployeesProyectsMapper {
    private EmployeesProyectsMapper() {

    }

    public static EmployeesProyectsResponse toResponse(EmployeesProyects entity) {
        if (entity == null) {
            return null;
        }
        return EmployeesProyectsResponse.builder()
                .employeeProjectsId(entity.getEmployeeProjectsId())
                .employeeId(entity.getEmployee().getEmployeeId())
                .employeeName(entity.getEmployee().getName())   // asumiendo getName() en Employee
                .projectId(entity.getProyect().getProjectId())
                .projectName(entity.getProyect().getName())     // asumiendo getName() en Proyect
                .build();
    }

    public static EmployeesProyects toEntity(EmployeesProyectsRequest dto, Employee employee, Proyect proyect) {
        if (dto == null || employee == null || proyect == null) {
            return null;
        }
        return EmployeesProyects.builder()
                .employee(employee)
                .proyect(proyect)
                .build();
    }
}
