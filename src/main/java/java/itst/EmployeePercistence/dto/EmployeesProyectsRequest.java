package java.itst.EmployeePercistence.dto;

import jakarta.validation.constraints.NotNull;

public class EmployeesProyectsRequest {
    @NotNull
    private Integer employeeId;

    @NotNull
    private Integer projectId;
    
}
