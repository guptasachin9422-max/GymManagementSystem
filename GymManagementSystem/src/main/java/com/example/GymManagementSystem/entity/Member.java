package com.example.GymManagementSystem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import java.util.List;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;
    private Integer age;
    private String phone;
    private String membershipType;

    @ManyToOne
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    public Trainer getTrainer(){
        return trainer;
    }
    public void setTrainer(Trainer trainer){
        this.trainer = trainer;
    }

    public Member(){}
    public  Integer getId(){
        return id;

    }

    public void setId(Integer id){
        this.id = id;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    public Integer getAge(){
        return age;
    }
    public void setAge(int age){
        this.age = age;
    }
    public String getPhone(){
        return phone;
    }
    public void setPhone(String phone){
        this.phone = phone;
    }
    public String getMembershipType(){
        return membershipType;
    }
    public void setMembershipType(String  membershipType){
        this.membershipType = membershipType;
    }
}
