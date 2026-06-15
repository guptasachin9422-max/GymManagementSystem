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
    private List<Trainer> allTrainers;

    public Trainer saveTrainer(Trainer trainer){
        return trainerRepository.save(trainer);
    }
    public List<Trainer> getAllTrainer(){
        return trainerRepository.findAll();
    }
    public Trainer getTrainerById(Long id){
        return trainerRepository.findById(id).orElse(null);
    }
    public Trainer updateTrainer(Long id , Trainer trainer){
        Trainer existingTrainer = trainerRepository.findById(id).orElse(null);

        if(existingTrainer != null){
            existingTrainer.setName(trainer.getName());
            existingTrainer.setSpecialty(trainer.getSpecialty());
            existingTrainer.setPhone(trainer.getPhone());

            return trainerRepository.save(existingTrainer);
        }
        return null;
    }
    public String deleteTrainer(Long id){
        trainerRepository.deleteById(id);

        return "Trainer Delete Successfully";
    }

    public List<Trainer> getAllTrainers() {
        return allTrainers;
    }

    public void setAllTrainers(List<Trainer> allTrainers) {
        this.allTrainers = allTrainers;
    }
}
