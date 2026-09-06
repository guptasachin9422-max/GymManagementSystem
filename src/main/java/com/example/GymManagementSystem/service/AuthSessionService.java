package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.AuthSession;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.AuthSessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Optional;

@Service
public class AuthSessionService {

    public static final long SESSION_HOURS = 12;

    private final AuthSessionRepository sessionRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthSessionService(AuthSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public AuthSession create(User user, String token, Instant createdAt) {
        AuthSession session = new AuthSession();
        session.setUser(user);
        session.setSessionTokenHash(hash(token));
        session.setCreatedAt(createdAt);
        session.setExpiresAt(createdAt.plus(SESSION_HOURS, ChronoUnit.HOURS));
        session.setActive(true);
        return sessionRepository.save(session);
    }

    public boolean hasActiveSession(Integer userId, Instant now) {
        return sessionRepository.findActiveByUserId(userId, now).isPresent();
    }

    @Transactional
    public boolean isValid(String token, Integer userId, Instant now) {
        Optional<AuthSession> result = sessionRepository.findBySessionTokenHash(hash(token));
        if (result.isEmpty()) return false;
        AuthSession session = result.get();
        if (!session.isActive() || !session.getUser().getId().equals(userId)
                || !session.getExpiresAt().isAfter(now)) {
            if (session.isActive() && !session.getExpiresAt().isAfter(now)) {
                session.setActive(false);
                sessionRepository.save(session);
            }
            return false;
        }
        return true;
    }

    @Transactional
    public void invalidate(String token) {
        sessionRepository.findBySessionTokenHash(hash(token)).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
        });
    }

    @Transactional
    public void deleteForUser(Integer userId) {
        sessionRepository.deleteByUser_Id(userId);
    }

    @Scheduled(fixedDelay = 15 * 60 * 1000L)
    @Transactional
    public void deactivateExpiredSessions() {
        sessionRepository.deactivateExpired(Instant.now());
    }

    public String generateOpaqueToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) result.append(String.format("%02x", value));
            return result.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash authentication session", e);
        }
    }
}
