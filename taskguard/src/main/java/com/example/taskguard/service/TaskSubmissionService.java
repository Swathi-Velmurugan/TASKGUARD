package com.example.taskguard.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.taskguard.entity.ProjectTask;
import com.example.taskguard.entity.SystemAccount;
import com.example.taskguard.entity.TaskSubmission;
import com.example.taskguard.entity.enums.CompletionStatus;
import com.example.taskguard.exception.ResourceNotFoundException;
import com.example.taskguard.repository.ProjectTaskRepository;
import com.example.taskguard.repository.SystemAccountRepository;
import com.example.taskguard.repository.TaskSubmissionRepository;

import jakarta.transaction.Transactional;

@Service
public class TaskSubmissionService {

    final TaskSubmissionRepository repository;
    final ProjectTaskRepository taskRepository;
    final SystemAccountRepository systemAccountRepository;

    public TaskSubmissionService(TaskSubmissionRepository repository,
            ProjectTaskRepository taskRepository,
            SystemAccountRepository systemAccountRepository) {
        this.repository = repository;
        this.taskRepository = taskRepository;
        this.systemAccountRepository = systemAccountRepository;
    }

    // CREATE
    @Transactional
    public TaskSubmission submitWork(TaskSubmission submission,
            Long taskId, Long contributorId) {
        ProjectTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + taskId));
        SystemAccount contributor = systemAccountRepository.findById(contributorId)
                .orElseThrow(() -> new ResourceNotFoundException("Contributor not found with id " + contributorId));
        submission.setTask(task);
        submission.setContributor(contributor);
        return repository.save(submission);
    }

    // READ ALL with optional task filter
    public List<TaskSubmission> listSubmissions(Long taskId) {
        if (taskId != null) {
            return repository.findByTaskId(taskId);
        }
        return repository.findAll();
    }

    // GET BY ID
    public TaskSubmission getSubmissionById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id " + id));
    }

    // REVIEW - manager updates feedback and status
    @Transactional
    public TaskSubmission reviewSubmission(Long id, String feedback, CompletionStatus completionStatus) {
        TaskSubmission existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id " + id));
        existing.setReviewerFeedback(feedback);
        existing.setCompletionStatus(completionStatus);
        return repository.save(existing);
    }

    // DELETE
    @Transactional
    public void deleteSubmission(Long id) {
        TaskSubmission existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found with id " + id));
        repository.delete(existing);
    }
}