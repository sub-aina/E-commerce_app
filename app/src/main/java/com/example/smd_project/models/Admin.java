package com.example.smd_project.models;

public class Admin {
    private String adminId;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String profileImageUrl;
    private long memberSince;
    private boolean notificationsEnabled;
    private boolean emailNotificationsEnabled;
    private String theme;
    private String language;

    public Admin() {
    }

    public Admin(String adminId, String name, String email, String phone) {
        this.adminId = adminId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = "System Administrator";
        this.memberSince = System.currentTimeMillis();
        this.notificationsEnabled = true;
        this.emailNotificationsEnabled = true;
        this.theme = "light";
        this.language = "English";
    }

    // Getters and Setters
    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getMemberSince() {
        return memberSince;
    }

    public void setMemberSince(long memberSince) {
        this.memberSince = memberSince;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}