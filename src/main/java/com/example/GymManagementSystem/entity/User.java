package com.example.GymManagementSystem.entity;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String password;

    private String role;

    private String displayName;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String trainerName;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String trainerSpecialty;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String trainerPhone;

    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Integer trainerAge;

    @Column(unique = true, nullable = false)
    private String email;


    // Constructor
    public User() {
    }


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

    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(String trainerName) {
        this.trainerName = trainerName;
    }

    public String getTrainerSpecialty() {
        return trainerSpecialty;
    }

    public void setTrainerSpecialty(String trainerSpecialty) {
        this.trainerSpecialty = trainerSpecialty;
    }

    public String getTrainerPhone() {
        return trainerPhone;
    }

    public void setTrainerPhone(String trainerPhone) {
        this.trainerPhone = trainerPhone;
    }

    public Integer getTrainerAge() {
        return trainerAge;
    }

    public void setTrainerAge(Integer trainerAge) {
        this.trainerAge = trainerAge;
    }
}
