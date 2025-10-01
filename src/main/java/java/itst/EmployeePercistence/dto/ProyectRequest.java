package java.itst.EmployeePercistence.dto;

import java.util.Date;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProyectRequest {

    @NotNull
    @Size(max = 100)
    private String proyectName;

    @NotNull
    @Past
    private Date startDate;

    @NotNull
    @Past
    private Date endDate;
}