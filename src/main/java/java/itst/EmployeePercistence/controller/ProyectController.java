package java.itst.EmployeePercistence.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.itst.EmployeePercistence.dto.ProyectRequest;
import java.itst.EmployeePercistence.dto.ProyectResponse;
import java.itst.EmployeePercistence.service.ProyectService;


import java.net.URI;
import java.util.List;



@RestController
@RequestMapping("/api/v1/proyects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT})
public class ProyectController {

    private final ProyectService service;

    @GetMapping
    public List<ProyectResponse> findAllList() {
        return service.findAllList();
    }

    @GetMapping("/{proyectId}")
    public ProyectResponse getById(@PathVariable int proyectId) {
        return service.findById(proyectId);
    }

    @PostMapping
    public ResponseEntity<ProyectResponse> add(@Valid @RequestBody ProyectRequest req) {
        ProyectResponse created = service.add(req);
        return ResponseEntity
                .created(URI.create("/api/v1/proyects/" + created.getProyectId()))
                .body(created);
    }

    @PutMapping("/{proyectId}")
    public ProyectResponse update(@PathVariable int proyectId, @Valid @RequestBody ProyectRequest req) {
        return service.update(proyectId, req);
    }

    @DeleteMapping("/{proyectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int proyectId) {
        service.delete(proyectId);
    }
}