package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.LogoutService;
import com.example.GymManagementSystem.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class LogoutController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogoutService logoutService;


    // ===========================
    // Logout
    // ===========================
    @PostMapping("/logout")
    public Object logout(
            @RequestHeader("Authorization") String authHeader) {

        // Authenticate user
        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        // Extract token
        String token = authHeader.substring(7).trim();

        // Blacklist token
        logoutService.blacklistToken(token);

        return "Logout Successful";
    }
}