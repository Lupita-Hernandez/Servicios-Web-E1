package java.itst.EmployeePercistence.model;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    private UUID id;
    private String name;
    private String lastname;
    private String department;
    
}
