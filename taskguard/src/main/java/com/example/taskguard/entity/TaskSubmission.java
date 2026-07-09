package com.example.taskguard.entity;

import java.time.LocalDateTime;

import com.example.taskguard.entity.enums.CompletionStatus;

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
@Table(name="task_submission")
public class TaskSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name="task_id",nullable=false)
    private ProjectTask task;
    @ManyToOne
    @JoinColumn(name="contributor_id",nullable=false)
    private SystemAccount contributor;
    @Column(name="hours_spent",nullable=false)
    private Integer hoursSpent;
    @Column(name="submission_notes",columnDefinition="TEXT",nullable=false)
    private String submissionNotes;
    @Column(name="reviewer_feedback",columnDefinition="TEXT",nullable=true)
    private String reviewerFeedback;
    @Enumerated(EnumType.STRING)
    @Column(name = "completion_status", nullable = false)
    private CompletionStatus completionStatus;
    @Column(name="submitted_at",nullable=false,updatable=false)
    private LocalDateTime submittedAt;
    public TaskSubmission() {
    }
    public TaskSubmission(Long id, ProjectTask task, SystemAccount contributor, Integer hoursSpent,
            String submissionNotes, String reviewerFeedback, CompletionStatus completionStatus, LocalDateTime submittedAt) {
        this.id = id;
        this.task = task;
        this.contributor = contributor;
        this.hoursSpent = hoursSpent;
        this.submissionNotes = submissionNotes;
        this.reviewerFeedback = reviewerFeedback;
        this.completionStatus = completionStatus;
        this.submittedAt = submittedAt;
    }
    @PrePersist
    public void onCreate(){
        submittedAt=LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public ProjectTask getTask() {
        return task;
    }
    public void setTask(ProjectTask task) {
        this.task = task;
    }
    public SystemAccount getContributor() {
        return contributor;
    }
    public void setContributor(SystemAccount contributor) {
        this.contributor = contributor;
    }
    public Integer getHoursSpent() {
        return hoursSpent;
    }
    public void setHoursSpent(Integer hoursSpent) {
        this.hoursSpent = hoursSpent;
    }
    public String getSubmissionNotes() {
        return submissionNotes;
    }
    public void setSubmissionNotes(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }
    public String getReviewerFeedback() {
        return reviewerFeedback;
    }
    public void setReviewerFeedback(String reviewerFeedback) {
        this.reviewerFeedback = reviewerFeedback;
    }
    public CompletionStatus getCompletionStatus() {
        return completionStatus;
    }
    public void setCompletionStatus(CompletionStatus completionStatus) {
        this.completionStatus = completionStatus;
    }
    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
    
    
}

