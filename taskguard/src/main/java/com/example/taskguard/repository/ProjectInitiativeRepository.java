package com.example.taskguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.taskguard.entity.ProjectInitiative;
import com.example.taskguard.entity.enums.InitiativeStatus;

@Repository
public interface ProjectInitiativeRepository extends JpaRepository<ProjectInitiative,Long>{
    boolean existsByProjectCode(String ProjectCode);
    List<ProjectInitiative> findByStatus(InitiativeStatus Status);
    List<ProjectInitiative> findByTitleContainingIgnoreCase(String title);
}
