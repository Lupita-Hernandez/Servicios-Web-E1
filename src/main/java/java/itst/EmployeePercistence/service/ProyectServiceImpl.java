package java.itst.EmployeePercistence.service;

import org.springframework.stereotype.Service;

import java.itst.EmployeePercistence.dto.ProyectRequest;
import java.itst.EmployeePercistence.dto.ProyectResponse;
import java.itst.EmployeePercistence.model.Proyect;
import java.itst.EmployeePercistence.repository.ProyectRepository;
import java.itst.EmployeePercistence.mapper.ProyectMapper;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProyectServiceImpl implements ProyectService {

    private final ProyectRepository repository;

    @Override
    public List<ProyectResponse> findAllList() {
        return repository.findAll().stream()
                .map(ProyectMapper::toResponse)
                .toList();
    }

    @Override
    public ProyectResponse findById(int proyectId) {
        Proyect proyect = repository.findById(proyectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyect not found: " + proyectId));
        return ProyectMapper.toResponse(proyect);
    }

    @Override
    public ProyectResponse add(ProyectRequest request) {
        Proyect saved = repository.save(ProyectMapper.toEntity(request));
        return ProyectMapper.toResponse(saved);
    }

    @Override
    public ProyectResponse update(int proyectId, ProyectRequest dto) {
        Proyect existing = repository.findById(proyectId)
                .orElseThrow(() -> new EntityNotFoundException("Proyect not found: " + proyectId));
        ProyectMapper.copyToEntity(dto, existing);
        Proyect saved = repository.save(existing);
        return ProyectMapper.toResponse(saved);
    }

    @Override
    public void delete(int proyectId) {
        if (!repository.existsById(proyectId)) {
            throw new EntityNotFoundException("Proyect not found: " + proyectId);
        }
        repository.deleteById(proyectId);
    }
}