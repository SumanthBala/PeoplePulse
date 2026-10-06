package com.example.employeeapp.controller;

import com.example.employeeapp.entity.Project;
import com.example.employeeapp.repository.ProjectRepository;
import com.example.employeeapp.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectRepository repo;
    private final EmployeeService employeeService;

    public ProjectController(ProjectRepository repo, EmployeeService employeeService) {
        this.repo = repo;
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<Map<String, Object>> all() {
        return repo.findAll().stream().map(p -> {
            BigDecimal spent = employeeService.spent(p.getId());
            return Map.<String, Object>of(
                    "id", p.getId(),
                    "name", p.getName(),
                    "budget", p.getBudget(),
                    "spent", spent,
                    "remaining", p.getBudget().subtract(spent),
                    "skills", p.getSkills() == null ? "" : p.getSkills(),
                    "jobDescription", p.getJobDescription() == null ? "" : p.getJobDescription(),
                    "jobRole", p.getJobRole() == null ? "" : p.getJobRole(),
                    "location", p.getLocation() == null ? "" : p.getLocation(),
                    "experience", p.getExperience() == null ? "" : p.getExperience()
            );
        }).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Project create(@Valid @RequestBody Project project) {
        project.setName(project.getName().trim());
        project.setSkills(normalizeSkills(project.getSkills()));
        project.setJobDescription(project.getJobDescription());
        project.setJobRole(project.getJobRole());
        project.setLocation(project.getLocation());
        project.setExperience(project.getExperience());
        if (project.getSkills() == null || project.getSkills().isBlank()) throw new com.example.employeeapp.exception.ApiException("At least one required skill is needed for a job");
        return repo.saveAndFlush(project);
    }

    @PutMapping("/{id}")
    public Project update(@PathVariable Long id, @Valid @RequestBody Project project) {
        Project existing = repo.findById(id).orElseThrow(() -> new com.example.employeeapp.exception.ApiException("Project not found"));
        existing.setName(project.getName().trim());
        existing.setBudget(project.getBudget());
        existing.setSkills(normalizeSkills(project.getSkills()));
        existing.setJobDescription(project.getJobDescription());
        existing.setJobRole(project.getJobRole());
        existing.setLocation(project.getLocation());
        existing.setExperience(project.getExperience());
        if (existing.getSkills() == null || existing.getSkills().isBlank()) throw new com.example.employeeapp.exception.ApiException("At least one required skill is needed for a job");
        return repo.saveAndFlush(existing);
    }

    private String normalizeSkills(String skills) {
        if (skills == null) return "";
        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .distinct()
                .collect(java.util.stream.Collectors.joining(", "));
    }
}
