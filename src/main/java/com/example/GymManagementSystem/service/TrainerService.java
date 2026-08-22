package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.repository.MemberRepository;
import com.example.GymManagementSystem.repository.TrainerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TrainerService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrainerRepository trainerRepository;


    // ==========================
    // Add Trainer
    // ==========================
    public Trainer saveTrainer(Trainer trainer) {
        return trainerRepository.save(trainer);
    }


    // ==========================
    // Get All Trainers
    // ==========================
    public List<Trainer> getAllTrainers() {
        return trainerRepository.findAll();
    }


    // ==========================
    // Get Trainer By Id
    // ==========================
    public Trainer getTrainerById(Long id) {
        return trainerRepository.findById(id).orElse(null);
    }


    // ==========================
    // Get Trainer By User Id
    // ==========================
    public Trainer getTrainerByUserId(Integer userId) {
        return trainerRepository.findByUserId(userId);
    }


    // ==========================
    // Update Trainer
    // ==========================
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


    // ==========================
    // Delete Trainer
    // ==========================
    @Transactional
    public String deleteTrainer(Long trainerId) {

        Trainer trainer =
                trainerRepository.findById(trainerId).orElse(null);

        if (trainer == null) {
            return "Trainer Not Found";
        }

        // Trainer ke saare members ko detach karo
        // Isse members delete nahi honge.
        // Sirf trainer_id NULL ho jayega.

        for (Member member : trainer.getMembers()) {

            member.setTrainer(null);

            memberRepository.save(member);
        }

        // Ab trainer delete karo
        trainerRepository.delete(trainer);

        return "Trainer Deleted Successfully";
    }
}