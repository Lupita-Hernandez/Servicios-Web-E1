package java.itst.EmployeePercistence.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "proyects")

public class Proyect {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "project_id", nullable = false, length = 20)
  private int proyectId;
  
  @Column(name = "project_name", nullable = false, length = 100)
  private String proyectName;

  @Column(name = "start_date", nullable = false)
  private Date startDate;
  
  @Column(name = "end_date", nullable = false) 
  private Date endDate;
}
