package com.example.GymManagementSystem.dto;

public class TrainerProfileResponse {

    private Long trainerId;
    private Integer userId;
    private String name;
    private String email;
    private String phone;
    private Integer age;
    private String specialty;

    public TrainerProfileResponse(Long trainerId, Integer userId, String name,
                                  String email, String phone, Integer age,
                                  String specialty) {
        this.trainerId = trainerId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.age = age;
        this.specialty = specialty;
    }

    public Long getTrainerId() { return trainerId; }
    public Integer getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Integer getAge() { return age; }
    public String getSpecialty() { return specialty; }
}
