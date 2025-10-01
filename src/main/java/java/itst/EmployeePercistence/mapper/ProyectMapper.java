package java.itst.EmployeePercistence.mapper;

import java.itst.EmployeePercistence.dto.ProyectRequest;
import java.itst.EmployeePercistence.dto.ProyectResponse;
import java.itst.EmployeePercistence.model.Proyect;

public final class ProyectMapper {

    public static ProyectResponse toResponse(Proyect proyect) {
        if (proyect == null)
            return null;
        return ProyectResponse.builder()
                .proyectId(proyect.getProyectId())
                .nameProyect(proyect.getProyectName())
                .startDate(proyect.getStartDate())
                .endDate(proyect.getEndDate())
                .build();
    }

    public static Proyect toEntity(ProyectRequest dto) {
        if (dto == null)
            return null;
        return Proyect.builder()
                .proyectName(dto.getProyectName())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

    public static void copyToEntity(ProyectRequest dto, Proyect entity) {
        if (dto == null || entity == null)
            return;
        entity.setProyectName(dto.getProyectName());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
    }
}