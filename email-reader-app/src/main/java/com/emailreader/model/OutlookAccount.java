package com.emailreader.model;

import java.time.LocalDateTime;

public class OutlookAccount {
    private Long id;
    private String email;
    private String displayName;
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiresAt;
    private Long addedByAdminId;

    public OutlookAccount() {}

    public OutlookAccount(Long id, String email, String displayName, String accessToken, 
                         String refreshToken, LocalDateTime expiresAt, Long addedByAdminId) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.addedByAdminId = addedByAdminId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Long getAddedByAdminId() {
        return addedByAdminId;
    }

    public void setAddedByAdminId(Long addedByAdminId) {
        this.addedByAdminId = addedByAdminId;
    }

    public boolean isTokenExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    @Override
    public String toString() {
        return "OutlookAccount{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", expiresAt=" + expiresAt +
                ", addedByAdminId=" + addedByAdminId +
                '}';
    }
}
