package com.example.taskguard.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.taskguard.entity.enums.InitiativeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="project_initiative")
public class ProjectInitiative {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="project_code",nullable=false,unique=true,length=50)
    private String projectCode;
    @Column(nullable=false,length=200)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name="budget_allocated",nullable=false,precision=15,scale=2)
    private BigDecimal budgetAllocated;
    @Column(name="budget_consumed",nullable=false,precision=15,scale=2)
    private BigDecimal budgetConsumed;
    @Column(name="start_date",nullable=false)
    private LocalDate startDate;
    @Column(name="target_end_date",nullable=false)
    private LocalDate targetEndDate;
    @ManyToOne
    @JoinColumn(name="director_id",nullable=false)
    private SystemAccount director;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InitiativeStatus status;
    @Column(name="created_at",nullable=false,updatable=false)
    private LocalDateTime createdAt;
    @Column(name="updated_at",nullable=true)
    private LocalDateTime updatedAt;
    public ProjectInitiative() {
    }
    public ProjectInitiative(Long id, String projectCode, String title, String description, BigDecimal budgetAllocated,
            BigDecimal budgetConsumed, LocalDate startDate, LocalDate targetEndDate, InitiativeStatus status,
            SystemAccount director) {
        this.id = id;
        this.projectCode = projectCode;
        this.title = title;
        this.description = description;
        this.budgetAllocated = budgetAllocated;
        this.budgetConsumed = budgetConsumed;
        this.startDate = startDate;
        this.targetEndDate = targetEndDate;
        this.status = status;
        this.director = director;
    }
    @PrePersist
    public void onCreate(){
        createdAt=LocalDateTime.now();
        if(budgetConsumed==null){
            budgetConsumed=BigDecimal.ZERO;
        }
    }
    @PreUpdate
    public void onUpdate(){
        updatedAt=LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getProjectCode() {
        return projectCode;
    }
    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public BigDecimal getBudgetAllocated() {
        return budgetAllocated;
    }
    public void setBudgetAllocated(BigDecimal budgetAllocated) {
        this.budgetAllocated = budgetAllocated;
    }
    public BigDecimal getBudgetConsumed() {
        return budgetConsumed;
    }
    public void setBudgetConsumed(BigDecimal budgetConsumed) {
        this.budgetConsumed = budgetConsumed;
    }
    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    public LocalDate getTargetEndDate() {
        return targetEndDate;
    }
    public void setTargetEndDate(LocalDate targetEndDate) {
        this.targetEndDate = targetEndDate;
    }
    public SystemAccount getDirector() {
        return director;
    }
    public void setDirector(SystemAccount director) {
        this.director = director;
    }
    public InitiativeStatus getStatus() {
        return status;
    }
    public void setStatus(InitiativeStatus status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    

}

