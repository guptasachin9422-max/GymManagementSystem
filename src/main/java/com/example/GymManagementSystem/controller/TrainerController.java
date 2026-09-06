package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.dto.TrainerResponse;
import com.example.GymManagementSystem.dto.TrainerUpdateRequest;
import com.example.GymManagementSystem.dto.TrainerProfileResponse;
import com.example.GymManagementSystem.service.TrainerService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/trainers")
public class TrainerController {

    @Autowired
    private TrainerService trainerService;

    @Autowired
    private UserService userService;

    // ==========================
    // Add Trainer (OWNER)
    // ==========================
    @PostMapping
    public Object addTrainer(
            @RequestBody Trainer trainer,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return trainerService.saveTrainer(trainer);
    }

    // ==========================
    // Get All Trainers
    // ==========================
    @GetMapping
    public Object getAllTrainers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        return trainerService.getAllTrainers();
    }

    // ==========================
    // Get Trainer By Id
    // ==========================
    @GetMapping("/{id:\\d+}")
    public Object getTrainerById(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        return trainerService.getTrainerById(id);
    }

    // ==========================
    // Update Trainer
    // ==========================
    @PutMapping("/{id:\\d+}")
    public Object updateTrainer(
            @PathVariable Long id,
            @RequestBody TrainerUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return trainerService.updateTrainer(id, request);
    }

    // ==========================
    // Delete Trainer
    // ==========================
    @DeleteMapping("/{id:\\d+}")
    public Object deleteTrainer(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return trainerService.deleteTrainer(id);
    }

    // ==========================
    // Trainer My Profile
    // ==========================
    @GetMapping("/my-profile")
    public Object myProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        if (!user.getRole().equalsIgnoreCase("TRAINER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return trainerService.getTrainerByUserId(user.getId());
    }

    @PutMapping("/my-profile")
    public Object updateMyProfile(
            @RequestBody TrainerUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }
        if (!user.getRole().equalsIgnoreCase("TRAINER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return trainerService.updateMyProfile(user.getId(), request);
    }
}



