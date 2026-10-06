package com.example.employeeapp.config;

import com.example.employeeapp.entity.Employee;
import com.example.employeeapp.entity.Project;
import com.example.employeeapp.repository.EmployeeRepository;
import com.example.employeeapp.repository.ProjectRepository;
import com.example.employeeapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner load(ProjectRepository projects, EmployeeRepository employees, UserRepository users, JdbcTemplate jdbc) {
        return args -> {
            // Existing H2 databases created before DB_ADMIN was added can still
            // contain the old enum CHECK constraint (ADMIN, MANAGER, CANDIDATE).
            // Remove only CHECK constraints from app_users so the new DB_ADMIN
            // role can be persisted without deleting or recreating any data.
            repairRoleConstraint(jdbc);
            repairSecurityAndProfileSchema(jdbc);
            if (!users.existsByUsername("admin")) users.save(new com.example.employeeapp.entity.User("admin","admin123",com.example.employeeapp.entity.User.Role.ADMIN,"System Admin"));
            if (!users.existsByUsername("manager")) users.save(new com.example.employeeapp.entity.User("manager","manager123",com.example.employeeapp.entity.User.Role.MANAGER,"Hiring Manager"));
            if (!users.existsByUsername("candidate")) users.save(new com.example.employeeapp.entity.User("candidate","candidate123",com.example.employeeapp.entity.User.Role.CANDIDATE,"Demo Candidate"));
            if (!users.existsByUsername("dbadmin")) users.save(new com.example.employeeapp.entity.User("dbadmin","dbadmin123",com.example.employeeapp.entity.User.Role.DB_ADMIN,"DB Persistence Admin"));
            Project phoenix = projects.findAll().stream()
                    .filter(p -> "Phoenix".equalsIgnoreCase(p.getName()))
                    .findFirst()
                    .orElseGet(() -> projects.save(new Project("Phoenix", new BigDecimal("500000"))));

            projects.findAll().stream()
                    .filter(p -> "Atlas".equalsIgnoreCase(p.getName()))
                    .findFirst()
                    .orElseGet(() -> projects.save(new Project("Atlas", new BigDecimal("300000"))));

            // Seed an employee independently of the project count.
            // This fixes the case where projects already existed but employees did not.
            if (!employees.existsById("PR-0001")) {
                Employee e = new Employee();
                e.setId("PR-0001");
                e.setName("Demo Employee");
                e.setEmail("demo@example.com");
                e.setCity("Hyderabad");
                e.setSalary(new BigDecimal("75000"));
                e.setProject(phoenix);
                employees.save(e);
            }
        };
    }

    private void repairSecurityAndProfileSchema(JdbcTemplate jdbc) {
        try {
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS ACCOUNT_LOCKED BOOLEAN DEFAULT FALSE");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS FAILED_LOGIN_ATTEMPTS INT DEFAULT 0");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS LOCKED_AT TIMESTAMP");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS PASSWORD_RESET_REQUIRED BOOLEAN DEFAULT FALSE");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS UNLOCK_REQUESTED_AT TIMESTAMP");
            jdbc.execute("ALTER TABLE LOGIN_ACTIVITY ADD COLUMN IF NOT EXISTS REASON VARCHAR(500)");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS FIRST_NAME VARCHAR(150)");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS LAST_NAME VARCHAR(150)");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(255)");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS PHONE VARCHAR(50)");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS SKILLS VARCHAR(3000)");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS PROFILE_PIC BLOB");
            jdbc.execute("ALTER TABLE APP_USERS ADD COLUMN IF NOT EXISTS PROFILE_PIC_CONTENT_TYPE VARCHAR(100)");
        } catch (Exception ignored) {
            // Hibernate ddl-auto=update remains responsible for fresh databases.
        }
    }

    private void repairRoleConstraint(JdbcTemplate jdbc) {
        try {
            jdbc.query(
                    "SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS " +
                    "WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='APP_USERS' AND CONSTRAINT_TYPE='CHECK'",
                    rs -> {
                        String constraint = rs.getString(1);
                        String safeName = constraint.replace("\"", "\"\"");
                        jdbc.execute("ALTER TABLE app_users DROP CONSTRAINT \"" + safeName + "\"");
                    });
        } catch (Exception ignored) {
            // Fresh databases may not have the old constraint. Nothing to repair.
        }
    }
}
