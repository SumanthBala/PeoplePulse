package com.example.employeeapp.controller;

import com.example.employeeapp.entity.Employee;
import com.example.employeeapp.entity.EmployeeRequest;
import com.example.employeeapp.entity.Project;
import com.example.employeeapp.exception.ApiException;
import com.example.employeeapp.repository.EmployeeRequestRepository;
import com.example.employeeapp.repository.ProjectRepository;
import com.example.employeeapp.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RestController
@RequestMapping("/api/employee-requests")
public class EmployeeRequestController {
    private final EmployeeRequestRepository requests;
    private final ProjectRepository projects;
    private final EmployeeService employeeService;

    public EmployeeRequestController(EmployeeRequestRepository requests, ProjectRepository projects, EmployeeService employeeService) {
        this.requests=requests; this.projects=projects; this.employeeService=employeeService;
    }

    @GetMapping
    public List<EmployeeRequest> all(){ return requests.findAllByOrderByRequestedAtDesc(); }

    @GetMapping("/manager/{username}")
    public List<EmployeeRequest> manager(@PathVariable String username){ return requests.findByRequestedByIgnoreCaseOrderByRequestedAtDesc(username); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeRequest create(@Valid @RequestBody EmployeeRequest r){
        if(r.getProjectId()==null) throw new ApiException("Project is required");
        Project p=projects.findById(r.getProjectId()).orElseThrow(()->new ApiException("Project not found"));
        r.setProjectName(p.getName());
        r.setStatus(EmployeeRequest.Status.PENDING);
        r.setReviewComment(null);
        return requests.saveAndFlush(r);
    }

    @PutMapping("/{id}/approve")
    @Transactional
    public Employee approve(@PathVariable Long id){
        EmployeeRequest r=requests.findById(id).orElseThrow(()->new ApiException("Employee request not found"));
        if(r.getStatus()!=EmployeeRequest.Status.PENDING) throw new ApiException("Only pending employee requests can be approved");
        Employee e=new Employee(); e.setName(r.getName()); e.setEmail(r.getEmail()); e.setCity(r.getCity()); e.setSalary(r.getSalary());
        Project p=projects.findById(r.getProjectId()).orElseThrow(()->new ApiException("Project not found")); e.setProject(p);
        Employee saved=employeeService.save(e);
        r.setStatus(EmployeeRequest.Status.APPROVED); r.setReviewComment("Approved by Admin"); requests.save(r);
        return saved;
    }

    @PutMapping("/{id}/reject")
    @Transactional
    public EmployeeRequest reject(@PathVariable Long id,@RequestBody Map<String,String> body){
        EmployeeRequest r=requests.findById(id).orElseThrow(()->new ApiException("Employee request not found"));
        if(r.getStatus()!=EmployeeRequest.Status.PENDING) throw new ApiException("Only pending employee requests can be rejected");
        String comment=body.getOrDefault("comment","").trim(); if(comment.isBlank()) throw new ApiException("Rejection reason is required");
        r.setStatus(EmployeeRequest.Status.REJECTED); r.setReviewComment(comment); return requests.saveAndFlush(r);
    }
}
