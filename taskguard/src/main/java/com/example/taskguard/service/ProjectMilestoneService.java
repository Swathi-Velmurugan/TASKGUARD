package com.example.taskguard.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.taskguard.entity.ProjectInitiative;
import com.example.taskguard.entity.ProjectMilestone;
import com.example.taskguard.entity.enums.MilestoneStatus;
import com.example.taskguard.exception.ResourceNotFoundException;
import com.example.taskguard.repository.ProjectInitiativeRepository;
import com.example.taskguard.repository.ProjectMilestoneRepository;
import com.example.taskguard.repository.ProjectTaskRepository;

import jakarta.transaction.Transactional;

@Service
public class ProjectMilestoneService {

    final ProjectMilestoneRepository repository;
    final ProjectInitiativeRepository initiativeRepository;
    final ProjectTaskRepository taskRepository;

    public ProjectMilestoneService(ProjectMilestoneRepository repository,
            ProjectInitiativeRepository initiativeRepository,
            ProjectTaskRepository taskRepository) {
        this.repository = repository;
        this.initiativeRepository = initiativeRepository;
        this.taskRepository = taskRepository;
    }

    // CREATE
    public ProjectMilestone createMilestone(ProjectMilestone milestone, Long initiativeId) {
        ProjectInitiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative not found with id " + initiativeId));
        milestone.setInitiative(initiative);
        return repository.save(milestone);
    }

    // READ ALL with optional filters
    public List<ProjectMilestone> listMilestones(String query, Long initiativeId, MilestoneStatus status) {
        List<ProjectMilestone> all = repository.findAll();

        return all.stream()
                .filter(m -> initiativeId == null || m.getInitiative().getId().equals(initiativeId))
                .filter(m -> status==null || m.getStatus().equals(status))
                .filter(m -> query == null || m.getTitle().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    // GET BY ID
    public ProjectMilestone getMilestoneById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id " + id));
    }

    // UPDATE
    public ProjectMilestone updateMilestone(Long id, ProjectMilestone updatedMilestone) {
        ProjectMilestone existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id " + id));
        existing.setTitle(updatedMilestone.getTitle());
        existing.setTargetDate(updatedMilestone.getTargetDate());
        existing.setAllocatedHours(updatedMilestone.getAllocatedHours());
        existing.setStatus(updatedMilestone.getStatus());
        return repository.save(existing);
    }

    // DELETE — tasks first, then milestone
    @Transactional
    public void deleteMilestone(Long id) {
        ProjectMilestone existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone not found with id " + id));
        taskRepository.deleteByMilestoneId(id);
        repository.delete(existing);
    }
}


