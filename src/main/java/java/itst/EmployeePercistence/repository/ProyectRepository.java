package java.itst.EmployeePercistence.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.itst.EmployeePercistence.model.Proyect;

public interface ProyectRepository extends JpaRepository<Proyect, Integer>  {
    
}
