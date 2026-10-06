package com.example.employeeapp.controller;

import com.example.employeeapp.dto.LoginRequest;
import com.example.employeeapp.entity.LoginActivity;
import com.example.employeeapp.entity.User;
import com.example.employeeapp.repository.LoginActivityRepository;
import com.example.employeeapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private final UserRepository users;
    private final LoginActivityRepository loginActivity;

    public AuthController(UserRepository users, LoginActivityRepository loginActivity) {
        this.users = users; this.loginActivity = loginActivity;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest r, HttpServletRequest request) { return authenticate(r, request, false); }

    @PostMapping("/db-login")
    public ResponseEntity<?> dbLogin(@RequestBody LoginRequest r, HttpServletRequest request) { return authenticate(r, request, true); }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String,String> body) {
        String username = body == null ? null : body.get("username");
        String currentPassword = body == null ? null : body.get("currentPassword");
        String newPassword = body == null ? null : body.get("newPassword");
        if (username == null || username.isBlank() || currentPassword == null || newPassword == null || newPassword.length() < 8)
            return ResponseEntity.badRequest().body(Map.of("message", "Username and a new password of at least 8 characters are required."));
        User user = users.findByUsername(username.trim()).orElse(null);
        if (user == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found."));
        if (!user.isPasswordResetRequired()) return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "A password reset is not currently required for this account."));
        if (!user.getPassword().equals(currentPassword)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Current password is incorrect."));
        user.setPassword(newPassword);
        user.setPasswordResetRequired(false);
        user.setFailedLoginAttempts(0);
        users.save(user);
        loginActivity.save(new LoginActivity(user.getUsername(), user.getRole().name(), "PASSWORD_CHANGED", null, "Password updated after administrator unlock"));
        return ResponseEntity.ok(Map.of("username", user.getUsername(), "name", user.getName(), "role", user.getRole().name(), "passwordResetRequired", false));
    }

    private ResponseEntity<?> authenticate(LoginRequest r, HttpServletRequest request, boolean dbOnly) {
        if (r == null || r.username() == null || r.password() == null)
            return ResponseEntity.badRequest().body(Map.of("message", "Username and password are required"));
        String username = r.username().trim();
        User user = users.findByUsername(username).orElse(null);
        String ip = request.getRemoteAddr();

        if (user == null) {
            loginActivity.save(new LoginActivity(username, "UNKNOWN", "FAILED", ip, "Unknown username"));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid username or password"));
        }
        if (user.isAccountLocked()) {
            loginActivity.save(new LoginActivity(username, user.getRole().name(), "LOCKED", ip, "Account is locked pending administrator approval"));
            return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of("message", "Account is locked. Administrator approval is required before a password reset."));
        }
        if (dbOnly && user.getRole() != User.Role.DB_ADMIN) {
            recordFailure(user, ip, "DB Persistence login attempted with a non-DB-admin account");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid DB Persistence credentials"));
        }
        if (!dbOnly && user.getRole() == User.Role.DB_ADMIN) {
            recordFailure(user, ip, "DB_ADMIN must use the DB Persistence login");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "DB Persistence account must use the DB Persistence login"));
        }
        if (!user.getPassword().equals(r.password())) {
            int attempts = recordFailure(user, ip, "Invalid password (failed attempt " + (user.getFailedLoginAttempts() + 1) + " of " + MAX_FAILED_ATTEMPTS + ")");
            if (attempts >= MAX_FAILED_ATTEMPTS)
                return ResponseEntity.status(HttpStatus.LOCKED).body(Map.of("message", "5 failed login attempts. Account locked and sent for administrator approval."));
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid password. Failed attempts: " + attempts + "/" + MAX_FAILED_ATTEMPTS));
        }

        user.setFailedLoginAttempts(0);
        users.save(user);
        String reason = user.isPasswordResetRequired() ? "Login accepted; administrator unlock requires a password change" : "Login successful";
        loginActivity.save(new LoginActivity(username, user.getRole().name(), "SUCCESS", ip, reason));
        if (user.isPasswordResetRequired()) {
            return ResponseEntity.ok(Map.of("username", user.getUsername(), "name", user.getName(), "role", user.getRole().name(), "passwordResetRequired", true));
        }
        return ResponseEntity.ok(Map.of("username", user.getUsername(), "name", user.getName(), "role", user.getRole().name(), "passwordResetRequired", false));
    }

    private int recordFailure(User user, String ip, String reason) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockedAt(LocalDateTime.now());
            user.setUnlockRequestedAt(LocalDateTime.now());
            reason = "5 failed login attempts - account automatically locked and unlock approval requested";
            loginActivity.save(new LoginActivity(user.getUsername(), user.getRole().name(), "LOCKED", ip, reason));
        } else {
            loginActivity.save(new LoginActivity(user.getUsername(), user.getRole().name(), "FAILED", ip, reason));
        }
        users.save(user);
        return attempts;
    }
}
