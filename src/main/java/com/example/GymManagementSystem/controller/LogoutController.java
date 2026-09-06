package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.LogoutService;
import com.example.GymManagementSystem.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.GymManagementSystem.service.AuthSessionService;

@RestController
@RequestMapping("/user")
public class LogoutController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogoutService logoutService;

    @Autowired
    private AuthSessionService authSessionService;

    // ===========================
    // Logout
    // ===========================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
        }

        // Authenticate user
        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        // Extract token
        String token = authHeader.substring(7).trim();

        // Blacklist token
        logoutService.blacklistToken(token);
        authSessionService.invalidate(token);

        return ResponseEntity.ok("Logout Successful");
    }
}

