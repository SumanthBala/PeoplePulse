package com.example.employeeapp.repository;

import com.example.employeeapp.entity.EmployeeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRequestRepository extends JpaRepository<EmployeeRequest, Long> {
    List<EmployeeRequest> findAllByOrderByRequestedAtDesc();
    List<EmployeeRequest> findByRequestedByIgnoreCaseOrderByRequestedAtDesc(String requestedBy);
}
