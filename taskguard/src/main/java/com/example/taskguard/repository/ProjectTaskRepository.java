package com.example.taskguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.taskguard.entity.ProjectTask;
import com.example.taskguard.entity.enums.TaskStatus;

import jakarta.transaction.Transactional;

@Repository
public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {

    Optional<ProjectTask> findByTaskCode(String taskCode);

    List<ProjectTask> findByInitiativeId(Long initiativeId);

    List<ProjectTask> findByAssigneeId(Long assigneeId);

    @Transactional
    @Modifying
    void deleteByInitiativeId(Long initiativeId);

    @Transactional
    @Modifying
    void deleteByMilestoneId(Long milestoneId);

    @Query("SELECT p FROM ProjectTask p WHERE " +
           "(:query IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.taskCode) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:assigneeId IS NULL OR p.assignee.id = :assigneeId)")
    List<ProjectTask> searchTasks(
            @Param("query") String query,
            @Param("status") TaskStatus status,
            @Param("assigneeId") Long assigneeId);

    @Query("SELECT SUM(p.loggedHours) FROM ProjectTask p")
    Long sumTotalLoggedHours();

    @Query("SELECT COUNT(p) FROM ProjectTask p WHERE p.status IN ('PENDING', 'IN_PROGRESS')")
    Long countActiveTasks();

    @Query("SELECT SUM(p.estimatedHours - p.loggedHours) FROM ProjectTask p " +
           "WHERE p.assignee.id = :assigneeId AND p.status IN ('PENDING', 'IN_PROGRESS')")
    Long calculateRemainingHoursForAssignee(@Param("assigneeId") Long assigneeId);
}

