package com.example.taskguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.example.taskguard.entity.ProjectMilestone;
import com.example.taskguard.entity.enums.MilestoneStatus;

import jakarta.transaction.Transactional;

@Repository
public interface ProjectMilestoneRepository extends JpaRepository<ProjectMilestone,Long> {
    List<ProjectMilestone> findByInitiativeIdAndStatus(Long initiativeId,  MilestoneStatus status);
    List<ProjectMilestone> findByInitiativeId(Long InitiativeId);
    @Transactional
    @Modifying
    void deleteByInitiativeId(Long InitiativeId);
}

