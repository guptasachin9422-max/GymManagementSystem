package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.service.TrainerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trainers")
public class TrainerController {

    @Autowired
    private TrainerService trainerService;

    @PostMapping
    public Trainer addTrainer(
            @RequestBody Trainer trainer) {

        return trainerService.saveTrainer(trainer);
    }

    @GetMapping
    public List<Map<String, Object>> getAllTrainers() {
        return trainerService.getAllTrainers();
    }

    @GetMapping("/{id}")
    public Trainer getTrainerById(
            @PathVariable Long id) {

        return trainerService.getTrainerById(id);
    }

    @GetMapping("/{trainerId}/members")
    public Trainer getTrainerMembers(
            @PathVariable Long trainerId) {

        return trainerService
                .getTrainerMembers(trainerId);
    }

    @PutMapping("/{id}")
    public Trainer updateTrainer(
            @PathVariable Long id,
            @RequestBody Trainer trainer) {

        return trainerService
                .updateTrainer(id, trainer);
    }

    @DeleteMapping("/{id}")
    public String deleteTrainer(
            @PathVariable Long id) {

        return trainerService.deleteTrainer(id);
    }
}
