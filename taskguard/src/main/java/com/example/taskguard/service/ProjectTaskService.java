package com.example.taskguard.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.taskguard.entity.ProjectInitiative;
import com.example.taskguard.entity.ProjectMilestone;
import com.example.taskguard.entity.ProjectTask;
import com.example.taskguard.entity.SystemAccount;
import com.example.taskguard.entity.enums.TaskStatus;
import com.example.taskguard.exception.CapacityExceededException;
import com.example.taskguard.exception.ResourceNotFoundException;
import com.example.taskguard.repository.ProjectInitiativeRepository;
import com.example.taskguard.repository.ProjectMilestoneRepository;
import com.example.taskguard.repository.ProjectTaskRepository;
import com.example.taskguard.repository.SystemAccountRepository;
import com.example.taskguard.repository.TaskSubmissionRepository;

import jakarta.transaction.Transactional;

@Service
public class ProjectTaskService {

    final ProjectTaskRepository repository;
    final ProjectInitiativeRepository initiativeRepository;
    final ProjectMilestoneRepository milestoneRepository;
    final SystemAccountRepository systemAccountRepository;
    final TaskSubmissionRepository submissionRepository;

    public ProjectTaskService(ProjectTaskRepository repository,
            ProjectInitiativeRepository initiativeRepository,
            ProjectMilestoneRepository milestoneRepository,
            SystemAccountRepository systemAccountRepository,
            TaskSubmissionRepository submissionRepository) {
        this.repository = repository;
        this.initiativeRepository = initiativeRepository;
        this.milestoneRepository = milestoneRepository;
        this.systemAccountRepository = systemAccountRepository;
        this.submissionRepository = submissionRepository;
    }

    // CREATE
    @Transactional
    public ProjectTask createTask(ProjectTask task, Long initiativeId,
            Long milestoneId, Long assigneeId) {

        // check taskCode uniqueness
        if (repository.findByTaskCode(task.getTaskCode()).isPresent()) {
            throw new CapacityExceededException("Task code already exists: " + task.getTaskCode());
        }

        // link initiative
        ProjectInitiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative not found with id " + initiativeId));
        task.setInitiative(initiative);

        // link milestone if provided
        if (milestoneId != null) {
            ProjectMilestone milestone = milestoneRepository.findById(milestoneId)
                    .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id " + milestoneId));
            task.setMilestone(milestone);
        }

        // link assignee if provided
        if (assigneeId != null) {
             if (task.getEstimatedHours() == null) {
                throw new IllegalArgumentException("Estimated hours cannot be null.");
            }

            validateAssigneeWorkloadCapacity(assigneeId, task.getEstimatedHours());
            SystemAccount assignee = systemAccountRepository.findById(assigneeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id " + assigneeId));
            task.setAssignee(assignee);
        }

        // default status
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.PENDING);
        }

        return repository.save(task);
    }

    // WORKLOAD CAPACITY CHECK
    public void validateAssigneeWorkloadCapacity(Long assigneeId, Integer upcomingHours) {
        Long existingLoad = repository.calculateRemainingHoursForAssignee(assigneeId);
        if (existingLoad == null) existingLoad = 0L;
        if (existingLoad + upcomingHours > 40) {
            throw new CapacityExceededException(
                "Resource workload constraint parameters blocked: " +
                "Requested assignment exceeds candidate's active maximum continuous operational limit of 40 weekly hours.");
        }
    }

    // READ ALL with filters
    public List<ProjectTask> listTasks(String query, TaskStatus status, Long assigneeId) {
        return repository.searchTasks(query, status, assigneeId);
    }

    // GET BY ID
    public ProjectTask getTaskById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
    }

    // UPDATE
    @Transactional
    public ProjectTask updateTask(Long id, ProjectTask updatedTask) {
        ProjectTask existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setPriority(updatedTask.getPriority());
        existing.setEstimatedHours(updatedTask.getEstimatedHours());
        existing.setStatus(updatedTask.getStatus());
        existing.setDueDate(updatedTask.getDueDate());
        return repository.save(existing);
    }

    // UPDATE STATUS ONLY
    @Transactional
    public ProjectTask updateTaskStatus(Long id, TaskStatus newStatus) {
        ProjectTask existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
        existing.setStatus(newStatus);
        return repository.save(existing);
    }

    // ASSIGN TASK
    @Transactional
    public ProjectTask assignTask(Long taskId, Long assigneeId) {
        ProjectTask existing = repository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + taskId));
        SystemAccount assignee = systemAccountRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignee not found with id " + assigneeId));

        int remainingHours = existing.getEstimatedHours() - existing.getLoggedHours();
        validateAssigneeWorkloadCapacity(assigneeId, remainingHours);

        existing.setAssignee(assignee);
        if(existing.getStatus()==TaskStatus.PENDING){
            existing.setStatus(TaskStatus.IN_PROGRESS);
        }
        return repository.save(existing);
    }

    // DELETE — submissions first, then task
    @Transactional
    public void deleteTask(Long id) {
        ProjectTask existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
        submissionRepository.deleteByTaskId(id);
        repository.delete(existing);
    }
}
