package com.example.GymManagementSystem.repository;

import com.example.GymManagementSystem.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository
        extends JpaRepository<Member, Integer> {

    List<Member> findByTrainerTrainerId(Long trainerId);

    Member findByUserId(Integer userId);

    List<Member> findByName(String name);

    List<Member> findByMembershipType(String membershipType);

    List<Member> findByAge(Integer age);

}