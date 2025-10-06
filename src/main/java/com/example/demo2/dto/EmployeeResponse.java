package com.example.demo2.dto;

import java.util.UUID;

public record EmployeeResponse(
UUID id,
String name,
String lastname,
String department
){}
