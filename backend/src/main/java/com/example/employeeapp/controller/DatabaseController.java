package com.example.employeeapp.controller;

import com.example.employeeapp.entity.LoginActivity;
import com.example.employeeapp.entity.User;
import com.example.employeeapp.repository.CandidateApplicationRepository;
import com.example.employeeapp.repository.EmployeeRepository;
import com.example.employeeapp.repository.EmployeeRequestRepository;
import com.example.employeeapp.repository.LoginActivityRepository;
import com.example.employeeapp.repository.ProjectRepository;
import com.example.employeeapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/db")
public class DatabaseController {
    private final EmployeeRepository employees; private final ProjectRepository projects;
    private final CandidateApplicationRepository applications; private final EmployeeRequestRepository employeeRequests;
    private final UserRepository users; private final LoginActivityRepository loginActivity; private final JdbcTemplate jdbc;
    @Value("${spring.datasource.url}") private String datasourceUrl;
    private static final Pattern READ_ONLY_SQL = Pattern.compile("^(SELECT|SHOW|EXPLAIN|VALUES)\\b", Pattern.CASE_INSENSITIVE);

    public DatabaseController(EmployeeRepository employees, ProjectRepository projects, CandidateApplicationRepository applications,
                              EmployeeRequestRepository employeeRequests, UserRepository users, LoginActivityRepository loginActivity, JdbcTemplate jdbc) {
        this.employees=employees; this.projects=projects; this.applications=applications; this.employeeRequests=employeeRequests;
        this.users=users; this.loginActivity=loginActivity; this.jdbc=jdbc;
    }
    @GetMapping("/status") public Map<String,Object> status(){
        Map<String,Object> r=new LinkedHashMap<>(); r.put("database","H2 file database"); r.put("datasourceUrl",datasourceUrl);
        r.put("employees",employees.count()); r.put("projects",projects.count()); r.put("candidateApplications",applications.count()); r.put("employeeRequests",employeeRequests.count()); r.put("users",users.count()); r.put("loginActivity",loginActivity.count());
        try(Connection c=Objects.requireNonNull(jdbc.getDataSource()).getConnection()){r.put("product",c.getMetaData().getDatabaseProductName());r.put("version",c.getMetaData().getDatabaseProductVersion());r.put("connected",!c.isClosed());}catch(Exception e){r.put("connected",false);r.put("connectionError",e.getMessage());}
        r.put("message","Counts are read directly from the running Spring Boot database connection."); return r;
    }
    @GetMapping("/tables") public List<Map<String,Object>> tables(){
        List<Map<String,Object>> result=new ArrayList<>();
        for(Map<String,Object> row:jdbc.queryForList("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' ORDER BY TABLE_NAME")){
            String table=String.valueOf(row.get("TABLE_NAME")); Map<String,Object> item=new LinkedHashMap<>(); item.put("table",table);
            try{item.put("rows",jdbc.queryForObject("SELECT COUNT(*) FROM \""+table+"\"",Long.class));}catch(Exception e){item.put("rows",0);} result.add(item);
        } return result;
    }
    @GetMapping("/login-activity") public Map<String,Object> loginActivity(){
        Map<String,Object> r=new LinkedHashMap<>(); r.put("successful",loginActivity.countByStatus("SUCCESS")); r.put("failed",loginActivity.countByStatus("FAILED")); r.put("locked",loginActivity.countByStatus("LOCKED")); r.put("entries",loginActivity.findTop50ByOrderByLoginTimeDesc()); return r;
    }
    @GetMapping("/unlock-requests") public List<Map<String,Object>> unlockRequests(){
        List<Map<String,Object>> result=new ArrayList<>();
        for(User u:users.findAll()) if(u.isAccountLocked()) result.add(userMap(u));
        result.sort((a,b)->String.valueOf(b.get("lockedAt")).compareTo(String.valueOf(a.get("lockedAt")))); return result;
    }
    @PutMapping("/unlock-requests/{username}/approve") public Map<String,Object> approveUnlock(@PathVariable String username){
        User u=users.findByUsername(username).orElseThrow(()->new IllegalArgumentException("User not found: "+username));
        if(!u.isAccountLocked()) return userMap(u);
        u.setAccountLocked(false); u.setFailedLoginAttempts(0); u.setPasswordResetRequired(true); u.setUnlockRequestedAt(null); users.save(u);
        loginActivity.save(new LoginActivity(u.getUsername(),u.getRole().name(),"UNLOCK_APPROVED",null,"Administrator approved unlock; password change required"));
        return userMap(u);
    }
    private Map<String,Object> userMap(User u){Map<String,Object> m=new LinkedHashMap<>();m.put("username",u.getUsername());m.put("name",u.getName());m.put("role",u.getRole().name());m.put("failedAttempts",u.getFailedLoginAttempts());m.put("accountLocked",u.isAccountLocked());m.put("passwordResetRequired",u.isPasswordResetRequired());m.put("lockedAt",u.getLockedAt());m.put("unlockRequestedAt",u.getUnlockRequestedAt());return m;}

    @PostMapping("/query") public Map<String,Object> query(@RequestBody Map<String,String> body){
        String sql=body==null?null:body.get("sql"); if(sql==null||sql.trim().isEmpty()) throw new IllegalArgumentException("SQL query is required.");
        String normalized=sql.trim(); if(normalized.endsWith(";")) normalized=normalized.substring(0,normalized.length()-1).trim(); if(normalized.contains(";")) throw new IllegalArgumentException("Only one SQL statement can be executed at a time.");
        if(!READ_ONLY_SQL.matcher(normalized).find()) throw new IllegalArgumentException("Read-only SQL is allowed: SELECT, SHOW, EXPLAIN or VALUES.");
        List<Map<String,Object>> rows=jdbc.queryForList(normalized); boolean truncated=rows.size()>500; if(truncated) rows=new ArrayList<>(rows.subList(0,500));
        LinkedHashSet<String> columns=new LinkedHashSet<>(); for(Map<String,Object> row:rows) columns.addAll(row.keySet()); Map<String,Object> r=new LinkedHashMap<>(); r.put("columns",new ArrayList<>(columns));r.put("rows",rows);r.put("rowCount",rows.size());r.put("truncated",truncated);return r;
    }
    @GetMapping("/analytics") public Map<String,Object> analytics(){
        Map<String,Object> r=new LinkedHashMap<>();
        r.put("employeesByCity",safeQuery("SELECT CITY AS CATEGORY, COUNT(*) AS VALUE FROM EMPLOYEES GROUP BY CITY ORDER BY VALUE DESC"));
        r.put("salaryByProject",safeQuery("SELECT P.NAME AS CATEGORY, COALESCE(SUM(E.SALARY), 0) AS SALARY, P.BUDGET AS BUDGET FROM PROJECTS P LEFT JOIN EMPLOYEES E ON E.PROJECT_ID = P.ID GROUP BY P.ID, P.NAME, P.BUDGET ORDER BY SALARY DESC"));
        r.put("applicationsByStatus",safeQuery("SELECT STATUS AS CATEGORY, COUNT(*) AS VALUE FROM CANDIDATE_APPLICATIONS GROUP BY STATUS ORDER BY VALUE DESC"));
        r.put("applicationsOverTime",safeQuery("SELECT CONCAT(YEAR(APPLIED_AT), '-', LPAD(MONTH(APPLIED_AT), 2, '0')) AS PERIOD, COUNT(*) AS VALUE FROM CANDIDATE_APPLICATIONS GROUP BY YEAR(APPLIED_AT), MONTH(APPLIED_AT) ORDER BY YEAR(APPLIED_AT), MONTH(APPLIED_AT)"));
        r.put("loginActivityOverTime",safeQuery("SELECT CONCAT(YEAR(LOGIN_TIME), '-', LPAD(MONTH(LOGIN_TIME), 2, '0'), '-', LPAD(DAY(LOGIN_TIME), 2, '0')) AS PERIOD, SUM(CASE WHEN STATUS='SUCCESS' THEN 1 ELSE 0 END) AS SUCCESSFUL, SUM(CASE WHEN STATUS='FAILED' THEN 1 ELSE 0 END) AS FAILED FROM LOGIN_ACTIVITY GROUP BY YEAR(LOGIN_TIME), MONTH(LOGIN_TIME), DAY(LOGIN_TIME) ORDER BY YEAR(LOGIN_TIME), MONTH(LOGIN_TIME), DAY(LOGIN_TIME)"));
        r.put("employeeRequestsByStatus",safeQuery("SELECT STATUS AS CATEGORY, COUNT(*) AS VALUE FROM EMPLOYEE_REQUESTS GROUP BY STATUS ORDER BY VALUE DESC")); return r;
    }
    private List<Map<String,Object>> safeQuery(String sql){try{return jdbc.queryForList(sql);}catch(Exception e){return Collections.emptyList();}}
}
