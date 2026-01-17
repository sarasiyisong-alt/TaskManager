package com.example.taskmanager.service;

import com.example.taskmanager.entity.Role;
import com.example.taskmanager.entity.User;
import com.example.taskmanager.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public User createUser(User user, User creator) {
        // Validation: Unique Username
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        // Hierarchy Checks
        if (creator.getRole() == Role.USER) {
            throw new RuntimeException("Users cannot create other users.");
        }
        if (creator.getRole() == Role.MANAGER) {
            if (user.getRole() == Role.ADMIN || user.getRole() == Role.MANAGER) {
                throw new RuntimeException("Managers can only create User role.");
            }
            user.setManager(creator); // Manager creates their own user
        }
        if (creator.getRole() == Role.ADMIN) {
            if (user.getRole() == Role.ADMIN) {
                throw new RuntimeException("Admins cannot create other Admins.");
            }
            // Admin can create Managers or Users (without specific manager, or assign
            // later)
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updates, User modifier) {
        User existing = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        // Permission Check
        if (modifier.getRole() == Role.MANAGER) {
            if (existing.getManager() == null || !existing.getManager().getId().equals(modifier.getId())) {
                throw new RuntimeException("Managers can only update their own users.");
            }
        }
        // Admin can update anyone

        if (updates.getEmail() != null && !updates.getEmail().isEmpty()) {
            existing.setEmail(updates.getEmail());
        }
        if (updates.getPassword() != null && !updates.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(updates.getPassword()));
        }

        return userRepository.save(existing);
    }

    public List<User> getManagedUsers(User manager) {
        if (manager.getRole() == Role.ADMIN) {
            return userRepository.findAll();
        } else if (manager.getRole() == Role.MANAGER) {
            return userRepository.findAll().stream()
                    .filter(u -> u.getManager() != null && u.getManager().getId().equals(manager.getId()))
                    .toList();
        }
        return List.of();
    }

    public void deleteUser(Long id, User modifier) {
        User target = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        if (modifier.getRole() == Role.MANAGER) {
            if (target.getManager() == null || !target.getManager().getId().equals(modifier.getId())) {
                throw new RuntimeException("Managers can only delete their own users.");
            }
        }
        userRepository.deleteById(id);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Initialize default users if not present
    @PostConstruct
    public void init() {
        if (userRepository.findByUsername("Admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("Admin");
            admin.setPassword(passwordEncoder.encode("password"));
            admin.setRole(Role.ADMIN);
            admin.setEmail("admin@example.com");
            userRepository.save(admin);
        }
        if (userRepository.findByUsername("manager").isEmpty()) {
            User manager = new User();
            manager.setUsername("manager");
            manager.setPassword(passwordEncoder.encode("password"));
            manager.setRole(Role.MANAGER);
            manager.setEmail("manager@example.com");
            userRepository.save(manager);
        }
        if (userRepository.findByUsername("user").isEmpty()) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("password"));
            user.setRole(Role.USER);
            user.setEmail("user@example.com");
            userRepository.save(user);
        }
    }
}
