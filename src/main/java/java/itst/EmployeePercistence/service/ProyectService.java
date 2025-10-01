package java.itst.EmployeePercistence.service;

import java.util.List;

import java.itst.EmployeePercistence.dto.ProyectRequest;
import java.itst.EmployeePercistence.dto.ProyectResponse;

public interface ProyectService {
    List<ProyectResponse> findAllList();
    ProyectResponse findById(int proyectId);
    ProyectResponse add(ProyectRequest request);
    ProyectResponse update(int proyectId, ProyectRequest request);
    void delete(int proyectId);
}