package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.dto.UpdateDisplayNameRequest;
import com.example.GymManagementSystem.dto.UserManagementResponse;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> getAllUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User requester = userService.authenticate(authHeader);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired session");
        }
        if (!"OWNER".equalsIgnoreCase(requester.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied");
        }
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}/display-name")
    public ResponseEntity<?> updateDisplayName(
            @PathVariable Integer id,
            @RequestBody UpdateDisplayNameRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User requester = userService.authenticate(authHeader);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired session");
        }
        if (!"OWNER".equalsIgnoreCase(requester.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return ResponseEntity.ok(userService.updateDisplayName(id, request.getDisplayName()));
    }

    // ==========================
    // Get User By Id
    // ==========================
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User requester = userService.authenticate(authHeader);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or expired session");
        }
        User user = userService.getUserById(id);
        return user == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(user);
    }

    // ==========================
    // Delete User
    // ==========================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User requester = userService.authenticate(authHeader);
        if (requester == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing authentication token");
        }
        if (!"OWNER".equalsIgnoreCase(requester.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Only the owner can delete users");
        }

        return ResponseEntity.ok(userService.deleteUser(id));
    }

}

