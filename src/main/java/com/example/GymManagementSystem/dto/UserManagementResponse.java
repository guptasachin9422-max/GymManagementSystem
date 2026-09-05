package com.example.GymManagementSystem.dto;

public class UserManagementResponse {
    private Integer id;
    private String username;
    private String email;
    private String role;
    private String displayName;

    public UserManagementResponse(Integer id, String username, String email,
                                   String role, String displayName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.displayName = displayName;
    }

    public Integer getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getDisplayName() { return displayName; }
}
