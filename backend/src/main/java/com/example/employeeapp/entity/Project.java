package com.example.employeeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Entity
@Table(name="projects")
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String name;
    @DecimalMin("0.0") @Column(nullable=false, precision=14, scale=2)
    private BigDecimal budget;
    @Column(length=1000)
    private String skills;
    @Column(length=3000) private String jobDescription;
    private String jobRole;
    private String location;
    private String experience;

    public Project() {}
    public Project(String name, BigDecimal budget) { this.name=name; this.budget=budget; }
    public Long getId(){return id;} public String getName(){return name;} public void setName(String name){this.name=name;}
    public BigDecimal getBudget(){return budget;} public void setBudget(BigDecimal budget){this.budget=budget;}
    public String getSkills(){return skills;} public void setSkills(String skills){this.skills=skills;}
    public String getJobDescription(){return jobDescription;} public void setJobDescription(String v){jobDescription=v;}
    public String getJobRole(){return jobRole;} public void setJobRole(String v){jobRole=v;}
    public String getLocation(){return location;} public void setLocation(String v){location=v;}
    public String getExperience(){return experience;} public void setExperience(String v){experience=v;}
}
