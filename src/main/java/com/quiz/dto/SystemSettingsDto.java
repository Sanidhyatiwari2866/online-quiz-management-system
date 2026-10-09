package com.quiz.dto;

public class SystemSettingsDto {

    private String platformName = "QuizMaster Online Quiz Platform";
    private Integer defaultTimeLimitMinutes = 15;
    private Integer defaultPassingScore = 50;
    private Boolean allowRegistrations = true;
    private Boolean maintenanceMode = false;
    private String announcement = "Welcome to Galgotias University Quiz Evaluation Portal!";

    public SystemSettingsDto() {
    }

    public SystemSettingsDto(String platformName, Integer defaultTimeLimitMinutes, Integer defaultPassingScore, Boolean allowRegistrations, Boolean maintenanceMode, String announcement) {
        this.platformName = platformName;
        this.defaultTimeLimitMinutes = defaultTimeLimitMinutes;
        this.defaultPassingScore = defaultPassingScore;
        this.allowRegistrations = allowRegistrations;
        this.maintenanceMode = maintenanceMode;
        this.announcement = announcement;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public Integer getDefaultTimeLimitMinutes() {
        return defaultTimeLimitMinutes;
    }

    public void setDefaultTimeLimitMinutes(Integer defaultTimeLimitMinutes) {
        this.defaultTimeLimitMinutes = defaultTimeLimitMinutes;
    }

    public Integer getDefaultPassingScore() {
        return defaultPassingScore;
    }

    public void setDefaultPassingScore(Integer defaultPassingScore) {
        this.defaultPassingScore = defaultPassingScore;
    }

    public Boolean getAllowRegistrations() {
        return allowRegistrations;
    }

    public void setAllowRegistrations(Boolean allowRegistrations) {
        this.allowRegistrations = allowRegistrations;
    }

    public Boolean getMaintenanceMode() {
        return maintenanceMode;
    }

    public void setMaintenanceMode(Boolean maintenanceMode) {
        this.maintenanceMode = maintenanceMode;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }
}
