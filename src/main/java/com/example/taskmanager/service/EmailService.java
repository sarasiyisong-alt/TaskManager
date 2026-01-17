package com.example.taskmanager.service;

import com.example.taskmanager.entity.Task;
import com.example.taskmanager.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    public void sendTaskNotification(Task task) {
        // Notify Creator
        if (task.getCreateUser() != null && task.getCreateUser().getEmail() != null) {
            sendEmail(task.getCreateUser().getEmail(), "Task Created: " + task.getTitle(), buildEmailBody(task));
        }

        // Notify Assignee (if different from creator)
        if (task.getAssignedUser() != null && task.getAssignedUser().getEmail() != null) {
            if (task.getCreateUserId() == null || !task.getCreateUserId().equals(task.getAssignedUserId())) {
                sendEmail(task.getAssignedUser().getEmail(), "Task Assigned: " + task.getTitle(), buildEmailBody(task));
            }
        }
    }

    private void sendEmail(String to, String subject, String body) {
        System.out.println("Sending email to: " + to);

        if (mailSender != null) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(to);
                message.setSubject(subject);
                message.setText(body);
                message.setFrom("noreply@taskmanager.com");
                mailSender.send(message);
                System.out.println("Email sent successfully.");
            } catch (Exception e) {
                System.err.println("Failed to send email: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("MailSender is not configured!");
        }
    }

    private String buildEmailBody(Task task) {
        return String.format(
                "Task Details:\nID: %d\nTitle: %s\nDescription: %s\nStatus: %s\nCreated By: %s\nAssigned To: %s\nCreated Date: %s",
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreateUser() != null ? task.getCreateUser().getUsername() : "Unknown",
                task.getAssignedUser() != null ? task.getAssignedUser().getUsername() : "Unassigned",
                task.getCreatedDate());
    }
}
