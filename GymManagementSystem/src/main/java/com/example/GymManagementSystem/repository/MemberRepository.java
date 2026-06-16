package com.example.GymManagementSystem.repository;

import com.example.GymManagementSystem.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository
        extends JpaRepository<Member, Integer> {

    Member findByUserId(Integer userId);
}