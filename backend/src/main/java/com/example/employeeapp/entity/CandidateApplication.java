package com.example.employeeapp.entity;
import jakarta.persistence.*; import jakarta.validation.constraints.*; import java.time.LocalDateTime;
@Entity @Table(name="candidate_applications") public class CandidateApplication {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(unique=true,length=40) private String applicationCode;
 @NotBlank private String candidateName; @NotBlank private String candidateUsername; @Email @NotBlank private String email; @NotBlank private String phone; @NotBlank private String skills; @NotBlank private String appliedRole;
 private Long projectId; private String projectName;
 @Lob @Basic(fetch=FetchType.LAZY) @com.fasterxml.jackson.annotation.JsonIgnore private byte[] resumeData;
 private String resumeFileName; private String resumeContentType; @Column(length=3000) private String comment; @Column(length=3000) private String rejectionComment; @Column(length=3000) private String managerComment;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.PENDING; private LocalDateTime appliedAt=LocalDateTime.now();
 public enum Status { PENDING, MANAGER_APPROVED_REVIEW, ADMIN_L1_REVIEW, L1_COMPLETED_MANAGER_APPROVED, SELECTED, MANAGER_REJECTED, ADMIN_REJECTED }
 public CandidateApplication(){} public Long getId(){return id;} public String getApplicationCode(){return applicationCode;} public void setApplicationCode(String v){applicationCode=v;}
 public String getCandidateName(){return candidateName;} public void setCandidateName(String v){candidateName=v;} public String getCandidateUsername(){return candidateUsername;} public void setCandidateUsername(String v){candidateUsername=v;} public String getEmail(){return email;} public void setEmail(String v){email=v;} public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
 public byte[] getResumeData(){return resumeData;} public void setResumeData(byte[] v){resumeData=v;} public String getResumeFileName(){return resumeFileName;} public void setResumeFileName(String v){resumeFileName=v;} public String getResumeContentType(){return resumeContentType;} public void setResumeContentType(String v){resumeContentType=v;}
 public String getSkills(){return skills;} public void setSkills(String v){skills=v;} public String getAppliedRole(){return appliedRole;} public void setAppliedRole(String v){appliedRole=v;} public Long getProjectId(){return projectId;} public void setProjectId(Long v){projectId=v;} public String getProjectName(){return projectName;} public void setProjectName(String v){projectName=v;}
 public String getComment(){return comment;} public void setComment(String v){comment=v;} public String getRejectionComment(){return rejectionComment;} public void setRejectionComment(String v){rejectionComment=v;} public String getManagerComment(){return managerComment;} public void setManagerComment(String v){managerComment=v;} public Status getStatus(){return status;} public void setStatus(Status v){status=v;} public LocalDateTime getAppliedAt(){return appliedAt;} public void setAppliedAt(LocalDateTime v){appliedAt=v;}
}
