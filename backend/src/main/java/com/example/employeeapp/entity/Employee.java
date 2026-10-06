package com.example.employeeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name="employees")
public class Employee {
    @Id @Column(length=8)
    private String id;
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank private String city;
    @DecimalMin("0.0") @Column(nullable=false, precision=14, scale=2)
    private BigDecimal salary;
    @ManyToOne(optional=false, fetch=FetchType.EAGER)
    @JoinColumn(name="project_id") private Project project;

    public Employee() {}
    public String getId(){return id;} public void setId(String id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public String getCity(){return city;} public void setCity(String city){this.city=city;}
    public BigDecimal getSalary(){return salary;} public void setSalary(BigDecimal salary){this.salary=salary;}
    public Project getProject(){return project;} public void setProject(Project project){this.project=project;}
}
