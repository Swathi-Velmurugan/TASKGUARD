package com.example.taskguard.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.taskguard.entity.ProjectInitiative;
import com.example.taskguard.entity.SystemAccount;
import com.example.taskguard.entity.enums.InitiativeStatus;
import com.example.taskguard.exception.CapacityExceededException;
import com.example.taskguard.exception.ResourceNotFoundException;
import com.example.taskguard.repository.ProjectInitiativeRepository;
import com.example.taskguard.repository.ProjectMilestoneRepository;
import com.example.taskguard.repository.ProjectTaskRepository;
import com.example.taskguard.repository.SystemAccountRepository;

import jakarta.transaction.Transactional;

@Service
public class ProjectInitiativeService {
    final ProjectInitiativeRepository repository;
    final SystemAccountRepository systemAccountRepository;
    final ProjectMilestoneRepository milestoneRepository;
    final ProjectTaskRepository taskRepository;

    public ProjectInitiativeService(ProjectInitiativeRepository repository,
            SystemAccountRepository systemAccountRepository, ProjectMilestoneRepository milestoneRepository,
            ProjectTaskRepository taskRepository) {
        this.repository = repository;
        this.systemAccountRepository = systemAccountRepository;
        this.milestoneRepository = milestoneRepository;
        this.taskRepository = taskRepository;
    }

    //create
    public ProjectInitiative createInitiative(ProjectInitiative initiative,Long directorId) {
        if(repository.existsByProjectCode(initiative.getProjectCode())){
            throw new CapacityExceededException("Project code already exists:"+initiative.getProjectCode());
        }
        SystemAccount director=systemAccountRepository.findById(directorId)
            .orElseThrow(()->new ResourceNotFoundException("Director not found with id:"+directorId));
        initiative.setDirector(director);
        return repository.save(initiative);
    }

    //read all with optional filters
    public List<ProjectInitiative> listInitiatives(String query,InitiativeStatus status){
        if(status!=null){
            return repository.findByStatus(status);
        }
        if(query!=null&&!query.isEmpty()){
            return repository.findByTitleContainingIgnoreCase(query);
        }
        return repository.findAll();
    }

    // getbyid
    public ProjectInitiative getInitiativeById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("initiative not found" + id));
    }

    // update
    public ProjectInitiative updateInitiative(Long id, ProjectInitiative updatedInitiative) {
        ProjectInitiative existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("initiative not found" + id));
        existing.setTitle(updatedInitiative.getTitle());
        existing.setDescription(updatedInitiative.getDescription());
        existing.setBudgetAllocated(updatedInitiative.getBudgetAllocated());
        existing.setStartDate(updatedInitiative.getStartDate());
        existing.setTargetEndDate(updatedInitiative.getTargetEndDate());
        existing.setStatus(updatedInitiative.getStatus());
        return repository.save(existing);

    }

    //delete-cascade tasks and milestones first
    @Transactional
    public void deleteInitiative(Long id){
        ProjectInitiative existing=repository.findById(id)
            .orElseThrow(()->new ResourceNotFoundException("Initiative not found with id"+id));
        taskRepository.deleteByInitiativeId(id);
        milestoneRepository.deleteByInitiativeId(id);
        repository.delete(existing);
    }

}
