package com.example.taskmanager.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.EqualsAndHashCode;
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

    @Enumerated(EnumType.STRING)
    private Priority priority;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Column(name = "create_user_id")
    private Long createUserId;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now(java.time.ZoneOffset.UTC);

    @ManyToOne
    @JoinColumn(name = "assigned_user_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User assignedUser;

    @ManyToOne
    @JoinColumn(name = "create_user_id", insertable = false, updatable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User createUser;
}
