package com.example.taskguard.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taskguard.entity.ProjectMilestone;
import com.example.taskguard.entity.enums.MilestoneStatus;
import com.example.taskguard.service.ProjectMilestoneService;

@RestController
@RequestMapping("/api/milestones")
public class ProjectMilestoneController {

    final ProjectMilestoneService service;

    public ProjectMilestoneController(ProjectMilestoneService service) {
        this.service = service;
    }

    // POST /api/milestones?initiativeId=1
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @PostMapping
    public ResponseEntity<ProjectMilestone> createMilestone(
            @RequestBody ProjectMilestone milestone,
            @RequestParam Long initiativeId) {
        ProjectMilestone saved = service.createMilestone(milestone, initiativeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/milestones?query=abc&initiativeId=1&status=ALL
    @PreAuthorize("hasAnyRole('PROJECT_DIRECTOR','PROJECT_MANAGER','TEAM_CONTRIBUTOR')")
    @GetMapping
    public ResponseEntity<List<ProjectMilestone>> listMilestones(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long initiativeId,
            @RequestParam(required = false) MilestoneStatus status) {
        return ResponseEntity.ok(service.listMilestones(query, initiativeId, status));
    }

    // GET /api/milestones/1
    @PreAuthorize("hasAnyRole('PROJECT_DIRECTOR','PROJECT_MANAGER','TEAM_CONTRIBUTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectMilestone> getMilestoneById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getMilestoneById(id));
    }

    // PUT /api/milestones/1
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectMilestone> updateMilestone(
            @PathVariable Long id,
            @RequestBody ProjectMilestone milestone) {
        return ResponseEntity.ok(service.updateMilestone(id, milestone));
    }

    // DELETE /api/milestones/1
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMilestone(@PathVariable Long id) {
        service.deleteMilestone(id);
        return ResponseEntity.ok("Milestone deleted successfully");
    }
}

