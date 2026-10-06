package com.example.employeeapp.service;

import com.example.employeeapp.entity.Employee;
import com.example.employeeapp.entity.Project;
import com.example.employeeapp.exception.ApiException;
import com.example.employeeapp.repository.EmployeeRepository;
import com.example.employeeapp.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employees;
    private final ProjectRepository projects;

    public EmployeeService(EmployeeRepository employees, ProjectRepository projects) {
        this.employees = employees;
        this.projects = projects;
    }

    public List<Employee> all() {
        return employees.findAll();
    }

    @Transactional
    public Employee save(Employee e) {
        if (e.getId() == null || e.getId().isBlank()) e.setId(nextId());
        if (e.getProject() == null || e.getProject().getId() == null)
            throw new ApiException("Project is required");

        Project p = projects.findById(e.getProject().getId())
                .orElseThrow(() -> new ApiException("Project not found"));

        BigDecimal salary = e.getSalary() == null ? BigDecimal.ZERO : e.getSalary();
        BigDecimal current = employees.findAll().stream()
                .filter(x -> x.getProject() != null
                        && x.getProject().getId().equals(p.getId())
                        && !x.getId().equals(e.getId()))
                .map(Employee::getSalary)
                .filter(x -> x != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (current.add(salary).compareTo(p.getBudget()) > 0)
            throw new ApiException("Out of budget for project '" + p.getName()
                    + "'. Available: " + p.getBudget().subtract(current));

        e.setProject(p);
        e.setSalary(salary);
        return employees.saveAndFlush(e);
    }

    @Transactional
    public void delete(String id) {
        employees.deleteById(id);
        employees.flush();
    }

    private String nextId() {
        int max = employees.findAll().stream()
                .map(Employee::getId)
                .filter(x -> x != null && x.startsWith("PR-"))
                .map(x -> x.substring(3))
                .filter(x -> x.matches("\\d+"))
                .mapToInt(Integer::parseInt)
                .max().orElse(0);
        return String.format("PR-%04d", max + 1);
    }

    public BigDecimal spent(Long projectId) {
        return employees.findAll().stream()
                .filter(e -> e.getProject() != null && e.getProject().getId().equals(projectId))
                .map(Employee::getSalary)
                .filter(x -> x != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
