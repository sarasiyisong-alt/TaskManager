package com.example.taskmanager.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.PENDING;

    private Integer priority;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Column(name = "create_user_id")
    private Long createUserId;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "assigned_user_id", insertable = false, updatable = false)
    private User assignedUser;

    @ManyToOne
    @JoinColumn(name = "create_user_id", insertable = false, updatable = false)
    private User createUser;
}
