package com.example.employeeapp.repository;

import com.example.employeeapp.entity.CandidateApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CandidateApplicationRepository extends JpaRepository<CandidateApplication, Long> {
    List<CandidateApplication> findByEmailIgnoreCaseOrderByAppliedAtDesc(String email);
    List<CandidateApplication> findByCandidateUsernameIgnoreCaseOrderByAppliedAtDesc(String candidateUsername);
    boolean existsByEmailIgnoreCaseAndProjectId(String email, Long projectId);
}
