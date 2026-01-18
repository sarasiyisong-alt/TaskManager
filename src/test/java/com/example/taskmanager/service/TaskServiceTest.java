package com.example.taskmanager.service;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateTask() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.PENDING);

        User creator = new User();
        creator.setId(1L);
        creator.setRole(com.example.taskmanager.entity.Role.USER);

        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task createdTask = taskService.createTask(task, creator);

        assertNotNull(createdTask);
        assertEquals("Test Task", createdTask.getTitle());
        assertEquals(TaskStatus.PENDING, createdTask.getStatus());
        verify(taskRepository, times(1)).save(task);
        verify(emailService, times(1)).sendTaskNotification(any(Task.class));
    }

    @Test
    void testUpdateTaskStatus() {
        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.PENDING);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task updatedTask = taskService.updateTaskStatus(1L, TaskStatus.APPROVED);

        assertNotNull(updatedTask);
        assertEquals(TaskStatus.APPROVED, updatedTask.getStatus());
        verify(taskRepository, times(1)).save(task);
    }
}
