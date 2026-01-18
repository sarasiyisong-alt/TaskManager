package com.example.taskmanager.e2e;

import com.example.taskmanager.entity.Role;
import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;

import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class AdminTaskVisibilityTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        // Ensure Admin exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("password");
            admin.setRole(Role.ADMIN);
            userService.createUser(admin, admin); // Self-create/init
        }
        adminUser = userRepository.findByUsername("admin").get();

        // Create a regular user
        if (userRepository.findByUsername("testuser").isEmpty()) {
            User user = new User();
            user.setUsername("testuser");
            user.setPassword("password");
            user.setRole(Role.USER);
            // Admin creates user
            userService.createUser(user, adminUser);
        }
        regularUser = userRepository.findByUsername("testuser").get();
    }

    @Test
    void testAdminCanSeeSelfAssignedTask() {
        Task task = new Task();
        task.setTitle("Admin Task");
        task.setDescription("Task created by Admin for Admin");
        task.setAssignedUserId(adminUser.getId());

        taskService.createTask(task, adminUser);

        List<Task> tasks = taskService.getAllTasks(adminUser);
        boolean found = tasks.stream().anyMatch(t -> t.getTitle().equals("Admin Task"));

        assertTrue(found, "Admin should see their own task");
    }

    @Test
    void testAdminCanSeeUserAssignedTask() {
        Task task = new Task();
        task.setTitle("User Task");
        task.setDescription("Task created by Admin for User");
        task.setAssignedUserId(regularUser.getId());

        taskService.createTask(task, adminUser);

        List<Task> tasks = taskService.getAllTasks(adminUser);
        boolean found = tasks.stream().anyMatch(t -> t.getTitle().equals("User Task"));

        assertTrue(found, "Admin should see task assigned to others");
    }
}
