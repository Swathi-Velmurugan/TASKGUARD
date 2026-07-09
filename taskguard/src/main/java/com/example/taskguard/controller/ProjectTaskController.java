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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taskguard.entity.ProjectTask;
import com.example.taskguard.entity.enums.TaskStatus;
import com.example.taskguard.service.ProjectTaskService;

@RestController
@RequestMapping("/api/tasks")
public class ProjectTaskController {

    final ProjectTaskService service;

    public ProjectTaskController(ProjectTaskService service) {
        this.service = service;
    }

    // POST /api/tasks?initiativeId=1&milestoneId=1&assigneeId=1
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @PostMapping
    public ResponseEntity<ProjectTask> createTask(
            @RequestBody ProjectTask task,
            @RequestParam Long initiativeId,
            @RequestParam(required = false) Long milestoneId,
            @RequestParam(required = false) Long assigneeId) {
        ProjectTask saved = service.createTask(task, initiativeId, milestoneId, assigneeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/tasks?query=abc&status=ALL&assigneeId=1
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','TEAM_CONTRIBUTOR')")
    @GetMapping
    public ResponseEntity<List<ProjectTask>> listTasks(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long assigneeId) {
        return ResponseEntity.ok(service.listTasks(query, status, assigneeId));
    }

    // GET /api/tasks/1
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER','TEAM_CONTRIBUTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectTask> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getTaskById(id));
    }

    // PUT /api/tasks/1
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectTask> updateTask(
            @PathVariable Long id,
            @RequestBody ProjectTask task) {
        return ResponseEntity.ok(service.updateTask(id, task));
    }

    // PATCH /api/tasks/1/status?newStatus=IN_PROGRESS
    @PreAuthorize("hasRole('TEAM_CONTRIBUTOR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectTask> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus newStatus) {
        return ResponseEntity.ok(service.updateTaskStatus(id, newStatus));
    }

    // PATCH /api/tasks/1/assign?assigneeId=2
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<ProjectTask> assignTask(
            @PathVariable Long id,
            @RequestParam Long assigneeId) {
        return ResponseEntity.ok(service.assignTask(id, assigneeId));
    }

    // DELETE /api/tasks/1
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {
        service.deleteTask(id);
        return ResponseEntity.ok("Task deleted successfully");
    }
}

