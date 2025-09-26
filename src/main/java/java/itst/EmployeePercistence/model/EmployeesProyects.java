package java.itst.EmployeePercistence.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name= "employees_proyects")
public class EmployeesProyects {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_projects_id", nullable = false)
    private Integer employeeProjectsId;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Empleado employee;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Proyect proyect;
    
}
