package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.repository.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainerService {

    @Autowired
    private TrainerRepository trainerRepository;

    // Add Trainer
    public Trainer saveTrainer(Trainer trainer) {
        return trainerRepository.save(trainer);
    }

    // Get All Trainers
    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }

    // Get Trainer By Id
    public Trainer getTrainerById(Long id) {
        return trainerRepository.findById(id).orElse(null);
    }

    // Update Trainer
    public Trainer updateTrainer(Long id, Trainer trainer) {

        Trainer existingTrainer =
                trainerRepository.findById(id).orElse(null);

        if (existingTrainer != null) {

            existingTrainer.setName(trainer.getName());
            existingTrainer.setPhone(trainer.getPhone());
            existingTrainer.setSpecialty(trainer.getSpecialty());
            existingTrainer.setUser(trainer.getUser());

            return trainerRepository.save(existingTrainer);
        }

        return null;
    }

    // Delete Trainer
    public String deleteTrainer(Long id) {

        trainerRepository.deleteById(id);

        return "Trainer Deleted Successfully";
    }
    public Trainer getTrainerByUserId(Integer userId) {
    return trainerRepository.findByUserId(userId);
}
}