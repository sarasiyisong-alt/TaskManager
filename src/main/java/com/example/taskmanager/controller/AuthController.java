package com.example.taskmanager.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @org.springframework.beans.factory.annotation.Autowired
    private com.example.taskmanager.service.UserService userService;

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Map.of("authenticated", false);
        }

        com.example.taskmanager.entity.User user = userService.getUserByUsername(auth.getName());

        return Map.of(
                "authenticated", true,
                "id", user.getId(),
                "username", auth.getName(),
                "roles", auth.getAuthorities());
    }
}
