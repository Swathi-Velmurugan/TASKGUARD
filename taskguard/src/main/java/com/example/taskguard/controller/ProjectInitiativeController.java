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

import com.example.taskguard.entity.ProjectInitiative;
import com.example.taskguard.entity.enums.InitiativeStatus;
import com.example.taskguard.service.ProjectInitiativeService;

@RestController
@RequestMapping("/api/initiatives")
public class ProjectInitiativeController {

    final ProjectInitiativeService service;

    public ProjectInitiativeController(ProjectInitiativeService service) {
        this.service = service;
    }

    // POST /api/initiatives?directorId=1
    @PostMapping
    @PreAuthorize("hasRole('PROJECT_DIRECTOR')")
    public ResponseEntity<ProjectInitiative> createInitiative(
            @RequestBody ProjectInitiative initiative,
            @RequestParam Long directorId) {
        ProjectInitiative saved = service.createInitiative(initiative, directorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/initiatives?query=abc&status=ALL
    @GetMapping
    @PreAuthorize("hasAnyRole('PROJECT_DIRECTOR','PROJECT_MANAGER','TEAM_CONTRIBUTOR')")
    public ResponseEntity<List<ProjectInitiative>> listInitiatives(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) InitiativeStatus status) {
        return ResponseEntity.ok(service.listInitiatives(query, status));
    }

    // GET /api/initiatives/1
    @PreAuthorize("hasAnyRole('PROJECT_DIRECTOR','PROJECT_MANAGER','TEAM_CONTRIBUTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ProjectInitiative> getInitiativeById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getInitiativeById(id));
    }

    // PUT /api/initiatives/1
    @PreAuthorize("hasRole('PROJECT_DIRECTOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ProjectInitiative> updateInitiative(
            @PathVariable Long id,
            @RequestBody ProjectInitiative initiative) {
        return ResponseEntity.ok(service.updateInitiative(id, initiative));
    }

    // DELETE /api/initiatives/1
    @PreAuthorize("hasRole('PROJECT_DIRECTOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteInitiative(@PathVariable Long id) {
        service.deleteInitiative(id);
        return ResponseEntity.ok("Initiative deleted successfully");
    }
}
