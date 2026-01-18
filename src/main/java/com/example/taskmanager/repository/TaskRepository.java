package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    java.util.List<Task> findByAssignedUser(com.example.taskmanager.entity.User user);

    java.util.List<Task> findByCreateUser(com.example.taskmanager.entity.User user);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t WHERE t.createUserId = :userId OR t.assignedUserId = :userId")
    java.util.List<Task> findTasksForUser(@org.springframework.data.repository.query.Param("userId") Long userId);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t WHERE t.createUserId = :userId OR t.assignedUserId = :userId OR t.createUserId IN (SELECT u.id FROM User u WHERE u.manager.id = :userId)")
    java.util.List<Task> findTasksForManager(@org.springframework.data.repository.query.Param("userId") Long userId);
}
