package com.example.taskguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import com.example.taskguard.entity.TaskSubmission;

import jakarta.transaction.Transactional;

@Repository
public interface TaskSubmissionRepository extends JpaRepository<TaskSubmission,Long>{
    List<TaskSubmission> findByTaskId(Long taskId);
    @Transactional
    @Modifying
    void deleteByTaskId(Long taskId);
}
