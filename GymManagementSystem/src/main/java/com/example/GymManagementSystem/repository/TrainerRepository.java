package com.example.GymManagementSystem.repository;

import com.example.GymManagementSystem.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository
        extends JpaRepository<Trainer, Long> {

    Trainer findByUserId(Integer userId);
}