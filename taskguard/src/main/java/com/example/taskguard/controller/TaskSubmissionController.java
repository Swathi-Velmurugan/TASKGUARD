package com.example.taskguard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taskguard.entity.TaskSubmission;
import com.example.taskguard.entity.enums.CompletionStatus;
import com.example.taskguard.service.TaskSubmissionService;

@RestController
@RequestMapping("/api/submissions")
public class TaskSubmissionController {

    final TaskSubmissionService service;

    public TaskSubmissionController(TaskSubmissionService service) {
        this.service = service;
    }

    // POST /api/submissions?taskId=1&contributorId=1
    @PreAuthorize("hasRole('TEAM_CONTRIBUTOR')")
    @PostMapping
    public ResponseEntity<TaskSubmission> submitWork(
            @RequestBody TaskSubmission submission,
            @RequestParam Long taskId,
            @RequestParam Long contributorId) {
        TaskSubmission saved = service.submitWork(submission, taskId, contributorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/submissions?taskId=1
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','TEAM_CONTRIBUTOR')")
    @GetMapping
    public ResponseEntity<List<TaskSubmission>> listSubmissions(
            @RequestParam(required = false) Long taskId) {
        return ResponseEntity.ok(service.listSubmissions(taskId));
    }

    // GET /api/submissions/1
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','TEAM_CONTRIBUTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<TaskSubmission> getSubmissionById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getSubmissionById(id));
    }

    // PATCH /api/submissions/1/review
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @PatchMapping("/{id}/review")
    public ResponseEntity<TaskSubmission> reviewSubmission(
            @PathVariable Long id,
            @RequestParam String reviewerFeedback,
            @RequestParam CompletionStatus completionStatus) {
        return ResponseEntity.ok(service.reviewSubmission(id, reviewerFeedback, completionStatus));
    }

    // DELETE /api/submissions/1
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubmission(@PathVariable Long id) {
        service.deleteSubmission(id);
        return ResponseEntity.ok("Submission deleted successfully");
    }
}
