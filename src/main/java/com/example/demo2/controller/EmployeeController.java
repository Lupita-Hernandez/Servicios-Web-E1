package com.example.demo2.controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo2.dto.EmployeeRequest;
import com.example.demo2.dto.EmployeeResponse;
import com.example.demo2.service.EmployeeService;



@RestController
@RequestMapping("api/v1/employees")

public class EmployeeController {

    private final EmployeeService service;


    public EmployeeController(EmployeeService service){
        this.service = service;
    }


    @PostMapping
    public EmployeeResponse add(@RequestBody EmployeeRequest employeeRequest){
        return service.add(employeeRequest);
    }
    
}
