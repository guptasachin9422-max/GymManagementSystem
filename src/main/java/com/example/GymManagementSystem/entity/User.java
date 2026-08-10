package com.example.GymManagementSystem.entity;

import jakarta.persistence.*;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String password;

    private String role;
    @Column(unique = true, nullable = false)
    private String email;
    public String getEmail() {
    return email;
}

    public void setEmail(String email) {
        this.email = email;
    }

    public User(){}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    @Column(name = "access_token", length = 1000)
private String accessToken;

public String getAccessToken() {
    return accessToken;
}

public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
}
}