package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class LogoutService {

    @Autowired
    private UserRepository userRepository;

    // Blacklisted tokens
    private final Set<String> blacklistedTokens = new HashSet<>();


    // ===========================
    // Blacklist Token
    // ===========================

    public void blacklistToken(String token) {
        if (token != null && !token.isEmpty()) {
            blacklistedTokens.add(token);
        }
    }


    // ===========================
    // Check Blacklisted Token
    // ===========================

    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }


    // ===========================
    // Logout User
    // ===========================

    public void logoutUser(User user) {

        user.setAccessToken(null);

        userRepository.save(user);
    }
}