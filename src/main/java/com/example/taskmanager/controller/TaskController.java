package com.example.taskmanager.controller;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.TaskStatus;
import com.example.taskmanager.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private com.example.taskmanager.service.UserService userService;

    @PostMapping
    public Task createTask(@RequestBody Task task) {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
        com.example.taskmanager.entity.User user = userService.getUserByUsername(username);
        return taskService.createTask(task, user);
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @PutMapping("/{id}/approve")
    public Task approveTask(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        TaskStatus status = TaskStatus.valueOf(statusStr.toUpperCase());
        return taskService.updateTaskStatus(id, status);
    }

    @GetMapping("/export")
    public void exportTasks(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"tasks.csv\"");
        taskService.exportTasksToCsv(response.getWriter());
    }
}
