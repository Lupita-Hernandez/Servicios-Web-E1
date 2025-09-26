package java.itst.EmployeePercistence.controller;
import java.itst.EmployeePercistence.dto.EmployeesProyectsRequest;
import java.itst.EmployeePercistence.dto.EmployeesProyectsResponse;
import java.itst.EmployeePercistence.service.EmployeesProyectsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employees-projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class EmployeesProyectsController {

    private final EmployeesProyectsService service;

    @GetMapping
    public List<EmployeesProyectsResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public EmployeesProyectsResponse getById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<EmployeesProyectsResponse> create(@Valid @RequestBody EmployeesProyectsRequest request) {
        EmployeesProyectsResponse created = service.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/employees-projects/" + created.getEmployeeProjectsId()))
                .body(created);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
