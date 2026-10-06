package com.example.employeeapp.controller;
import com.example.employeeapp.entity.Employee;
import com.example.employeeapp.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/employees")
public class EmployeeController {
 private final EmployeeService service;
 public EmployeeController(EmployeeService service){this.service=service;}
 @GetMapping List<Employee> all(){return service.all();}
 @PostMapping Employee save(@Valid @RequestBody Employee e){return service.save(e);}
 @DeleteMapping("/{id}") void delete(@PathVariable String id){service.delete(id);}
}
