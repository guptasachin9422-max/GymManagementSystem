package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    // ==========================
    // Register
    // ==========================
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody User user) {

        return userService.register(user);
    }

    // ==========================
    // Login
    // ==========================
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody User user) {

        return userService.login(user);
    }

    // ==========================
    // Get All Users
    // ==========================
    @GetMapping
    public List<User> getAllUsers() {

        return userService.getAllUsers();
    }

    // ==========================
    // Get User By Id
    // ==========================
    @GetMapping("/{id}")
    public User getUserById(
            @PathVariable Integer id) {

        return userService.getUserById(id);
    }

    // ==========================
    // Delete User
    // ==========================
    @DeleteMapping("/{id}")
    public String deleteUser(
            @PathVariable Integer id) {

        return userService.deleteUser(id);
    }

}