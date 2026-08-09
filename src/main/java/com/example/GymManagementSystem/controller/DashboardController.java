package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.DashboardService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @GetMapping
    public Object getDashboard(
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return "Access Denied";
        }

        return dashboardService.getDashboard();
    }
}