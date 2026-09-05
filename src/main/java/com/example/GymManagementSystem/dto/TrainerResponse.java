package com.example.GymManagementSystem.dto;

public class TrainerResponse {

    private Long id;
    private String name;
    private String specialty;
    private String phone;
    private Integer age;
    private Integer userId;
    private String email;

    public TrainerResponse() {
    }

    public TrainerResponse(Long id, String name, String specialty, String phone,
                           Integer age, Integer userId, String email) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.phone = phone;
        this.age = age;
        this.userId = userId;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public String getPhone() {
        return phone;
    }

    public Integer getAge() {
        return age;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
