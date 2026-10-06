package com.example.employeeapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_activity")
public class LoginActivity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100) private String username;
    @Column(length = 30) private String role;
    @Column(nullable = false, length = 20) private String status;
    @Column(nullable = false) private LocalDateTime loginTime = LocalDateTime.now();
    @Column(length = 100) private String ipAddress;
    @Column(length = 500) private String reason;

    public LoginActivity() {}
    public LoginActivity(String username, String role, String status, String ipAddress) {
        this(username, role, status, ipAddress, null);
    }
    public LoginActivity(String username, String role, String status, String ipAddress, String reason) {
        this.username = username; this.role = role; this.status = status; this.ipAddress = ipAddress;
        this.reason = reason; this.loginTime = LocalDateTime.now();
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public LocalDateTime getLoginTime() { return loginTime; }
    public String getIpAddress() { return ipAddress; }
    public String getReason() { return reason; }
}
