package com.example.taskguard.entity;

import java.time.LocalDateTime;

import com.example.taskguard.entity.enums.DomainRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
@Entity
@Table(name = "system_account")
public class SystemAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Email(message="Email should be valid")
    @NotBlank(message="Email cannot be blank")
    @Column(nullable=false, unique=true, length=255)
    private String email;
    @Column(name="password_hash", nullable=false, length=255)
    private String passwordHash;
    @Column(name="full_name", nullable=false, length=150)
    private String fullName;
    @Enumerated(EnumType.STRING)
    @Column(name="domain_role", nullable=false, length=50)
    private DomainRole domainRole;
    @Column(name="is_active", nullable=false)
    private Boolean isActive;
    @Column(name="created_at", nullable=false, updatable=false)
    private LocalDateTime createdAt;
    public SystemAccount() {
    }
    public SystemAccount(Long id, String email, String passwordHash, String fullName, DomainRole domainRole,
            Boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.domainRole = domainRole;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
    @PrePersist
    public void onCreate(){
        createdAt=LocalDateTime.now();
        if(isActive==null){
            isActive=true;
        }
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public DomainRole getDomainRole() {
        return domainRole;
    }
    public void setDomainRole(DomainRole domainRole) {
        this.domainRole = domainRole;
    }
    public Boolean isActive() {
        return isActive;
    }
    public void setActive(Boolean isActive) {
        this.isActive = isActive;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}











/*1.	SystemAccount Entity (Table: system_account) 
Fields: id (Long, auto-generated primary key), email (String, @Column
 nullable=false, unique=true, length=255), passwordHash (String, @Column name="password_hash",
  nullable=false, length=255), fullName (String, @Column name="full_name", nullable=false,
   length=150), domainRole (String, @Column name="domain_role", nullable=false, length=50),
    isActive (Boolean, @Column name="is_active", nullable=false, default=true), createdAt 
    (LocalDateTime, @Column name="created_at", nullable=false, updatable=false, auto-set
     on @PrePersist). 
Note: Valid domainRole values are PROJECT_DIRECTOR, PROJECT_MANAGER, and TEAM_CONTRIBUTOR. 
The isActive flag is used to revoke access without deleting the account record. Password
 is stored only as a BCrypt hash in passwordHash; the raw password field is never persisted. 
*/