package java.itst.EmployeePercistence.dto;
import jakarta.validation.constraints.NotBlank;
public record EmployeeRequest (
    @NotBlank(message = "El nombre es obligatorio")
    String name,
    @NotBlank(message = "El apellido es obligatorio")
    String lastname,
    @NotBlank(message = "El apellido es obligatorio")
    String department
){}
