package com.example.taskguard.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.taskguard.entity.enums.MilestoneStatus;

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
import jakarta.persistence.Table;

@Entity
@Table(name="project_milestone")
public class ProjectMilestone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="initiative_id",nullable=false)
    private ProjectInitiative initiative;
    @Column(nullable=false,length=200)
    private String title;
    @Column(name="target_date",nullable=false)
    private LocalDate targetDate;
    @Column(name="allocated_hours",nullable=false)
    private Integer allocatedHours;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilestoneStatus status;
    @Column(name="created_at",nullable=false,updatable=false)
    private LocalDateTime createdAt;
    public ProjectMilestone() {
    }
    public ProjectMilestone(Long id, ProjectInitiative initiative, String title, LocalDate targetDate,
            Integer allocatedHours, MilestoneStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.initiative = initiative;
        this.title = title;
        this.targetDate = targetDate;
        this.allocatedHours = allocatedHours;
        this.status = status;
        this.createdAt = createdAt;
    }
    @PrePersist
    public void onCreate(){
        createdAt=LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public ProjectInitiative getInitiative() {
        return initiative;
    }
    public void setInitiative(ProjectInitiative initiative) {
        this.initiative = initiative;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public LocalDate getTargetDate() {
        return targetDate;
    }
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }
    public Integer getAllocatedHours() {
        return allocatedHours;
    }
    public void setAllocatedHours(Integer allocatedHours) {
        this.allocatedHours = allocatedHours;
    }
    public MilestoneStatus getStatus() {
        return status;
    }
    public void setStatus(MilestoneStatus status) {
        this.status = status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


}


