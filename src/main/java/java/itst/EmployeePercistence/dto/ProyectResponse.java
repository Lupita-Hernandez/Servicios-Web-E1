package java.itst.EmployeePercistence.dto;


import java.util.Date;

import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class ProyectResponse {
    int proyectId;
    String nameProyect;
    Date startDate;
    Date endDate;
}