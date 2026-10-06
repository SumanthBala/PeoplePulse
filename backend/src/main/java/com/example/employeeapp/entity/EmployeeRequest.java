package com.example.employeeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="employee_requests")
public class EmployeeRequest {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String name;
    @Email @NotBlank private String email;
    @NotBlank private String city;
    @DecimalMin("0.0") @Column(nullable=false, precision=14, scale=2) private BigDecimal salary;
    @Column(nullable=false) private Long projectId;
    private String projectName;
    @NotBlank private String requestedBy;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.PENDING;
    @Column(length=2000) private String reviewComment;
    private LocalDateTime requestedAt=LocalDateTime.now();

    public enum Status { PENDING, APPROVED, REJECTED }
    public EmployeeRequest() {}
    public Long getId(){return id;} public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getCity(){return city;} public void setCity(String v){city=v;}
    public BigDecimal getSalary(){return salary;} public void setSalary(BigDecimal v){salary=v;} public Long getProjectId(){return projectId;} public void setProjectId(Long v){projectId=v;}
    public String getProjectName(){return projectName;} public void setProjectName(String v){projectName=v;} public String getRequestedBy(){return requestedBy;} public void setRequestedBy(String v){requestedBy=v;}
    public Status getStatus(){return status;} public void setStatus(Status v){status=v;} public String getReviewComment(){return reviewComment;} public void setReviewComment(String v){reviewComment=v;}
    public LocalDateTime getRequestedAt(){return requestedAt;}
}
