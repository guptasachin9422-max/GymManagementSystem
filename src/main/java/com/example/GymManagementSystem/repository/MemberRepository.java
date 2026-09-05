package com.example.GymManagementSystem.repository;

import com.example.GymManagementSystem.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberRepository
        extends JpaRepository<Member, Integer> {

    List<Member> findByTrainerTrainerId(Long trainerId);

    @Query("select m from Member m left join fetch m.trainer where m.user.id = :userId")
    Member findByUserId(@Param("userId") Integer userId);

    List<Member> findByName(String name);

    List<Member> findByMembershipType(String membershipType);

    List<Member> findByAge(Integer age);

}
