package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        return userService.login(user);
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<?> getAllUsers(@PathVariable int userId) {
        return userService.getAllUsers(userId);
    }

    @GetMapping("/trainers/{userId}")
    public ResponseEntity<?> getAllTrainers(@PathVariable int userId) {
        return userService.getAllTrainers(userId);
    }

    @GetMapping("/details/{id}/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable int id,
                                         @PathVariable int userId) {
        return userService.getUserById(id, userId);
    }

    @DeleteMapping("/{id}/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable int id,
                                             @PathVariable int userId) {
        return userService.deleteUser(id, userId);
    }
}
