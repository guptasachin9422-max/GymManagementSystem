package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.TrainerService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return "Access Denied";
        }

        return trainerService.saveTrainer(trainer);
    }

    // ==========================
    // Get All Trainers
    // ==========================
    @GetMapping
    public Object getAllTrainers(
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        return trainerService.getAllTrainers();
    }

    // ==========================
    // Get Trainer By Id
    // ==========================
    @GetMapping("/{id}")
    public Object getTrainerById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        return trainerService.getTrainerById(id);
    }

    // ==========================
    // Update Trainer
    // ==========================
    @PutMapping("/{id}")
    public Object updateTrainer(
            @PathVariable Long id,
            @RequestBody Trainer trainer,
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return "Access Denied";
        }

        return trainerService.updateTrainer(id, trainer);
    }

    // ==========================
    // Delete Trainer
    // ==========================
    @DeleteMapping("/{id}")
    public Object deleteTrainer(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return "Access Denied";
        }

        return trainerService.deleteTrainer(id);
    }

    // ==========================
    // Trainer My Profile
    // ==========================
    @GetMapping("/my-profile")
    public Object myProfile(
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        if (!user.getRole().equalsIgnoreCase("TRAINER")) {
            return "Access Denied";
        }

        return trainerService.getTrainerByUserId(user.getId());
    }
}