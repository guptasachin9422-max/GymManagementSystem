package com.example.GymManagementSystem.repository;

import com.example.GymManagementSystem.entity.AuthSession;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface AuthSessionRepository extends JpaRepository<AuthSession, Long> {

    Optional<AuthSession> findBySessionTokenHash(String sessionTokenHash);

    @Query("select s from AuthSession s where s.user.id = :userId and s.active = true and s.expiresAt > :now")
    Optional<AuthSession> findActiveByUserId(@Param("userId") Integer userId, @Param("now") Instant now);

    @Modifying
    @Query("update AuthSession s set s.active = false where s.user.id = :userId and s.active = true")
    int deactivateByUserId(@Param("userId") Integer userId);

    @Modifying
    @Query("update AuthSession s set s.active = false where s.expiresAt <= :now and s.active = true")
    int deactivateExpired(@Param("now") Instant now);

    void deleteByUser_Id(Integer userId);
}
