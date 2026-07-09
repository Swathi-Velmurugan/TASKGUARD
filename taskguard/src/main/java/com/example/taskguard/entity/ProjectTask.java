package com.example.taskguard.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.taskguard.entity.enums.Priority;
import com.example.taskguard.entity.enums.TaskStatus;

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
@Table(name="project_task")
public class ProjectTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="task_code",nullable=false,unique=true,length=50)
    private String taskCode;
    @ManyToOne
    @JoinColumn(name="initiative_id",nullable=false)
    private ProjectInitiative initiative;
    @ManyToOne
    @JoinColumn(name="milestone_id",nullable=true)
    private ProjectMilestone milestone;
    @Column(nullable=false,length=200)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Priority priority;
    @Column(name="estimated_hours",nullable=false)
    private Integer estimatedHours;
    @Column(name="logged_hours",nullable=false)
    private Integer loggedHours;
    @Column(name="due_date",nullable=false)
    private LocalDate dueDate;
    @ManyToOne
    @JoinColumn(name="assigned_id",nullable=true)
    private SystemAccount assignee;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
    @Column(name="created_at",nullable=false,updatable=false)
    private LocalDateTime createdAt;
    @Column(name="updated_at",nullable=true)
    private LocalDateTime updatedAt;
    public ProjectTask() {
    }
    public ProjectTask(Long id, String taskCode, ProjectInitiative initiative, ProjectMilestone milestone, String title,
            String description, Priority priority, Integer estimatedHours, Integer loggedHours, LocalDate dueDate,
            SystemAccount assignee, TaskStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.taskCode = taskCode;
        this.initiative = initiative;
        this.milestone = milestone;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.estimatedHours = estimatedHours;
        this.loggedHours = loggedHours;
        this.dueDate = dueDate;
        this.assignee = assignee;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    @PrePersist
    public void onCreate(){
        createdAt=LocalDateTime.now();
        if(loggedHours==null){
            loggedHours=0;
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
    public String getTaskCode() {
        return taskCode;
    }
    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
    }
    public ProjectInitiative getInitiative() {
        return initiative;
    }
    public void setInitiative(ProjectInitiative initiative) {
        this.initiative = initiative;
    }
    public ProjectMilestone getMilestone() {
        return milestone;
    }
    public void setMilestone(ProjectMilestone milestone) {
        this.milestone = milestone;
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
    public Priority getPriority() {
        return priority;
    }
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    public Integer getEstimatedHours() {
        return estimatedHours;
    }
    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }
    public Integer getLoggedHours() {
        return loggedHours;
    }
    public void setLoggedHours(Integer loggedHours) {
        this.loggedHours = loggedHours;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    public SystemAccount getAssignee() {
        return assignee;
    }
    public void setAssignee(SystemAccount assignee) {
        this.assignee = assignee;
    }
    public TaskStatus getStatus() {
        return status;
    }
    public void setStatus(TaskStatus status) {
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


