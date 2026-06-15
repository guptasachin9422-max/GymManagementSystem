package com.example.GymManagementSystem.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "trainers")
public class Trainer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long trainerId;

    private String name;
    private String specialty;
    private String phone;

    public Trainer(){}
    public Trainer(Long trainerId , String name , String specialty , String phone){
        this.name = name;
        this.specialty = specialty;
        this.phone = phone;
        this.trainerId = trainerId;

    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
