package com.example.demo2.controller;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo2.dto.EmployeeRequest;
import com.example.demo2.dto.EmployeeResponse;
import com.example.demo2.service.EmployeeService;

import jakarta.validation.Valid;



@RestController
@RequestMapping("api/v1/employees")

public class EmployeeController {

    private final EmployeeService service;


    public EmployeeController(EmployeeService service){
        this.service = service;
    }


    //@PostMapping
    //public EmployeeResponse add(@RequestBody EmployeeRequest employeeRequest){
    //    return service.add(employeeRequest);
    //}

    @GetMapping("/{id}")
    public EmployeeResponse getById(@PathVariable int id){
        return service.getById(id);
    }

    @GetMapping
    public List<EmployeeResponse> getAll() {
        return service.getAll();
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse crear(@Valid @RequestBody EmployeeRequest request) {
        return service.add(request);
    }

    @PutMapping("/{id}")
    public EmployeeResponse update(@PathVariable UUID id,@Valid @RequestBody EmployeeRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID id) {
    service.deleteById(id);
}

    
}
