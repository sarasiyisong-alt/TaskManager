package com.example.taskmanager.service;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private com.example.taskmanager.repository.UserRepository userRepository;

    public Task createTask(Task task, com.example.taskmanager.entity.User creator) {
        task.setCreatedDate(LocalDateTime.now());
        task.setStatus(TaskStatus.PENDING);
        task.setCreateUserId(creator.getId());

        // Default assignment to creator if null
        if (task.getAssignedUserId() == null) {
            task.setAssignedUserId(creator.getId());
        }

        // Validate Assignment Rules
        validateAssignment(task, creator);

        Task savedTask = taskRepository.save(task);

        // Fetch full entities for email
        savedTask = taskRepository.findById(savedTask.getId()).orElse(savedTask);

        emailService.sendTaskNotification(savedTask);

        return savedTask;
    }

    private void validateAssignment(Task task, com.example.taskmanager.entity.User creator) {
        Long assigneeId = task.getAssignedUserId();
        if (assigneeId == null || assigneeId.equals(creator.getId()))
            return; // Self-assignment always ok

        if (creator.getRole() == com.example.taskmanager.entity.Role.USER) {
            throw new RuntimeException("Users can only assign tasks to themselves.");
        }

        if (creator.getRole() == com.example.taskmanager.entity.Role.MANAGER) {
            // Check if assignee is managed by this manager
            com.example.taskmanager.entity.User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));

            if (assignee.getManager() == null || !assignee.getManager().getId().equals(creator.getId())) {
                throw new RuntimeException("Managers can only assign tasks to their own users.");
            }
        }
    }

    public List<Task> getAllTasks(com.example.taskmanager.entity.User user) {
        if (user.getRole() == com.example.taskmanager.entity.Role.ADMIN) {
            return taskRepository.findAll();
        } else if (user.getRole() == com.example.taskmanager.entity.Role.MANAGER) {
            return taskRepository.findTasksForManager(user.getId());
        } else {
            return taskRepository.findTasksForUser(user.getId());
        }
    }

    public Task updateTaskStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus(status);
        return taskRepository.save(task);
    }

    public void exportTasksToCsv(java.io.Writer writer, com.example.taskmanager.entity.User user)
            throws java.io.IOException {
        List<Task> tasks = getAllTasks(user);
        try (java.io.PrintWriter printer = new java.io.PrintWriter(writer)) {
            writer.write('\uFEFF'); // Write BOM for Excel
            printer.println("Task ID,Title,Description,Status,Priority,Assigned User,Created Date");
            for (Task task : tasks) {
                printer.println(String.format("%d,%s,%s,%s,%s,%s,%s",
                        task.getId(),
                        escapeSpecialCharacters(task.getTitle()),
                        escapeSpecialCharacters(task.getDescription()),
                        task.getStatus(),
                        task.getPriority(),
                        task.getAssignedUser() != null ? escapeSpecialCharacters(task.getAssignedUser().getUsername())
                                : "Unassigned",
                        task.getCreatedDate()));
            }
        }
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public void deleteTask(Long id, com.example.taskmanager.entity.User requester) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new RuntimeException("Task not found"));

        // Permission Check: Admin or Creator only
        boolean isAdmin = requester.getRole() == com.example.taskmanager.entity.Role.ADMIN;
        boolean isCreator = task.getCreateUserId().equals(requester.getId());

        if (!isAdmin && !isCreator) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Only Admin or the Task Creator can delete this task.");
        }

        taskRepository.delete(task);
    }
}
