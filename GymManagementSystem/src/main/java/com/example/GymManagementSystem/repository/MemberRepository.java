package com.example.GymManagementSystem.repository;

import com.example.GymManagementSystem.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface MemberRepository
        extends JpaRepository<Member, Integer> {
    List<Member>findByTrainerTrainerId(Long trainerId);

    Member findByUserId(Integer userId);

}