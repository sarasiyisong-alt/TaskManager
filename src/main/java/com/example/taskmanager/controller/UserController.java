package com.example.taskmanager.controller;

import com.example.taskmanager.entity.User;
import com.example.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByUsername(username);
    }

    @GetMapping
    public List<User> getManagedUsers() {
        return userService.getManagedUsers(getCurrentUser());
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user, getCurrentUser());
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updates) {
        return userService.updateUser(id, updates, getCurrentUser());
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id, getCurrentUser());
    }
}
