package com.example.employeeapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name="app_users", uniqueConstraints=@UniqueConstraint(columnNames="username"))
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    @NotBlank private String username;
    @NotBlank private String password;
    @Enumerated(EnumType.STRING) @Column(nullable=false)
    private Role role;
    private String name;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    @Column(length=3000)
    private String skills;
    @Lob
    private byte[] profilePic;
    private String profilePicContentType;

    private int failedLoginAttempts = 0;

    private boolean accountLocked = false;

    private boolean passwordResetRequired = false;

    private LocalDateTime lockedAt;
    private LocalDateTime unlockRequestedAt;

    public enum Role { ADMIN, MANAGER, CANDIDATE, DB_ADMIN }
    public User() {}
    public User(String username,String password,Role role,String name){this.username=username;this.password=password;this.role=role;this.name=name;}
    public Long getId(){return id;} public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public Role getRole(){return role;} public void setRole(Role v){role=v;}
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getFirstName(){return firstName;} public void setFirstName(String v){firstName=v;}
    public String getLastName(){return lastName;} public void setLastName(String v){lastName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getSkills(){return skills;} public void setSkills(String v){skills=v;}
    public byte[] getProfilePic(){return profilePic;} public void setProfilePic(byte[] v){profilePic=v;}
    public String getProfilePicContentType(){return profilePicContentType;} public void setProfilePicContentType(String v){profilePicContentType=v;}
    public int getFailedLoginAttempts(){return failedLoginAttempts;} public void setFailedLoginAttempts(int v){failedLoginAttempts=v;}
    public boolean isAccountLocked(){return accountLocked;} public void setAccountLocked(boolean v){accountLocked=v;}
    public boolean isPasswordResetRequired(){return passwordResetRequired;} public void setPasswordResetRequired(boolean v){passwordResetRequired=v;}
    public LocalDateTime getLockedAt(){return lockedAt;} public void setLockedAt(LocalDateTime v){lockedAt=v;}
    public LocalDateTime getUnlockRequestedAt(){return unlockRequestedAt;} public void setUnlockRequestedAt(LocalDateTime v){unlockRequestedAt=v;}
}
