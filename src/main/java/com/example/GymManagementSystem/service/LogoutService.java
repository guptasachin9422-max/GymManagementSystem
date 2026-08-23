package com.example.GymManagementSystem.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class LogoutService {

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
}