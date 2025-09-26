package java.itst.EmployeePercistence.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.itst.EmployeePercistence.model.EmployeesProyects;

public interface EmployeesProyectsRepository extends JpaRepository<EmployeesProyects, Integer> {
    
}
