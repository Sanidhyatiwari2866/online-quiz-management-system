package com.quiz.dto;

import java.time.LocalDateTime;

public class SystemAlertDto {

    private String severity; // INFO, WARNING, DANGER, SUCCESS
    private String title;
    private String message;
    private LocalDateTime timestamp;

    public SystemAlertDto() {
        this.timestamp = LocalDateTime.now();
    }

    public SystemAlertDto(String severity, String title, String message) {
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
