package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.DashboardService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @GetMapping
    public Object getDashboard(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return dashboardService.getDashboard();
    }
}


