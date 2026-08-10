package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.service.LogoutService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class LogoutController {

    @Autowired
    private LogoutService logoutService;

    @Autowired
    private UserService userService;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

            return ResponseEntity.badRequest()
                    .body("Authorization token required");
        }

        String token = authHeader.substring(7);

        // Check whether token belongs to a valid user
        if (userService.authenticate(authHeader) == null) {

            return ResponseEntity.status(401)
                    .body("Invalid Token");
        }

        logoutService.logout(token);

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Logout Successful"
                )
        );
    }
}