package com.example.employeeapp.repository;

import com.example.employeeapp.entity.LoginActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoginActivityRepository extends JpaRepository<LoginActivity, Long> {
    List<LoginActivity> findTop50ByOrderByLoginTimeDesc();
    long countByStatus(String status);
}
