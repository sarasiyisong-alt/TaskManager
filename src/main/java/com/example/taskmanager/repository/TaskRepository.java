package com.example.taskmanager.repository;

import com.example.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    java.util.List<Task> findByAssignedUser(com.example.taskmanager.entity.User user);

    java.util.List<Task> findByCreateUser(com.example.taskmanager.entity.User user);
}
