package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.dto.TrainerResponse;
import com.example.GymManagementSystem.dto.TrainerUpdateRequest;
import com.example.GymManagementSystem.dto.TrainerProfileResponse;
import com.example.GymManagementSystem.repository.MemberRepository;
import com.example.GymManagementSystem.repository.TrainerRepository;
import com.example.GymManagementSystem.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
public class TrainerService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private UserRepository userRepository;

    // ==========================
    // Add Trainer
    // ==========================
    @Transactional
    public TrainerResponse saveTrainer(Trainer trainer) {
        if (trainer.getUser() != null && trainer.getUser().getId() != null) {
            User user = userRepository.findById(trainer.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Trainer existing = trainerRepository.findByUser_Id(user.getId());
            if (existing != null) return toResponse(existing);
            trainer.setUser(user);
        }
        Trainer saved = trainerRepository.save(trainer);
        System.out.printf("Trainer saved: userId=%s, trainerId=%d, name=%s%n",
                saved.getUser() == null ? null : saved.getUser().getId(),
                saved.getTrainerId(), saved.getName());
        return toResponse(saved);
    }


    // ==========================
    // Get All Trainers
    // ==========================
    @Transactional
    public List<TrainerResponse> getAllTrainers() {
        syncMissingTrainerProfiles();
        List<Trainer> trainers = trainerRepository.findAll();
        System.out.printf("GET /trainers returned %d trainers%n", trainers.size());
        return trainers.stream().map(this::toResponse).toList();
    }

    private void syncMissingTrainerProfiles() {
        for (User user : userRepository.findAll()) {
            if (!"TRAINER".equalsIgnoreCase(user.getRole())
                    || trainerRepository.findByUser_Id(user.getId()) != null) {
                continue;
            }
            Trainer trainer = new Trainer();
            String name = user.getDisplayName();
            if (name == null || name.isBlank()) name = user.getUsername();
            trainer.setName(name);
            trainer.setUser(user);
            trainerRepository.save(trainer);
            System.out.printf("Missing trainer profile repaired: userId=%d, trainerId=%d, name=%s%n",
                    user.getId(), trainer.getTrainerId(), trainer.getName());
        }
    }


    // ==========================
    // Get Trainer By Id
    // ==========================
    public TrainerResponse getTrainerById(Long id) {
        return trainerRepository.findById(id).map(this::toResponse).orElse(null);
    }


    // ==========================
    // Get Trainer By User Id
    // ==========================
    public TrainerProfileResponse getTrainerByUserId(Integer userId) {
        Trainer trainer = trainerRepository.findByUser_Id(userId);
        return trainer == null ? null : toProfileResponse(trainer);
    }

    @Transactional
    public TrainerProfileResponse updateMyProfile(Integer userId, TrainerUpdateRequest request) {
        Trainer trainer = trainerRepository.findByUser_Id(userId);
        if (trainer == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainer profile not found");
        }

        updateTrainer(trainer.getTrainerId(), request);
        Trainer updated = trainerRepository.findById(trainer.getTrainerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trainer profile not found"));
        return toProfileResponse(updated);
    }


    // ==========================
    // Update Trainer
    // ==========================
    @Transactional
    public TrainerResponse updateTrainer(Long id, TrainerUpdateRequest request) {
        Trainer existingTrainer = trainerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trainer not found"));

        String name = request.getName() == null ? "" : request.getName().trim();
        String specialty = request.getSpecialty() == null ? "" : request.getSpecialty().trim();
        String phone = request.getPhone() == null ? "" : request.getPhone().trim();
        if (name.isBlank() || specialty.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Name and specialty are required");
        }
        if (!phone.isBlank() && !phone.matches("^\\+?[0-9\\s()\\-]{7,20}$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Please enter a valid phone number");
        }

        User linkedUser = existingTrainer.getUser();
        if (linkedUser != null && request.getEmail() != null
                && !request.getEmail().trim().isBlank()) {
            String email = request.getEmail().trim();
            User emailOwner = userRepository.findByEmail(email).orElse(null);
            if (emailOwner != null && !emailOwner.getId().equals(linkedUser.getId())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Email is already in use");
            }
            linkedUser.setEmail(email);
            userRepository.save(linkedUser);
        }

        existingTrainer.setName(name);
        existingTrainer.setPhone(phone);
        existingTrainer.setSpecialty(specialty);
        existingTrainer.setAge(request.getAge());
        Trainer saved = trainerRepository.save(existingTrainer);
        System.out.printf("Trainer updated: trainerId=%d, userId=%s, name=%s%n",
                saved.getTrainerId(),
                linkedUser == null ? null : linkedUser.getId(),
                saved.getName());
        return toResponse(saved);
    }

    private TrainerResponse toResponse(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerResponse(
                trainer.getTrainerId(),
                effectiveName(trainer),
                trainer.getSpecialty(),
                trainer.getPhone(),
                trainer.getAge(),
                user == null ? null : user.getId(),
                user == null ? null : user.getEmail());
    }

    private TrainerProfileResponse toProfileResponse(Trainer trainer) {
        User user = trainer.getUser();
        return new TrainerProfileResponse(
                trainer.getTrainerId(),
                user == null ? null : user.getId(),
                effectiveName(trainer),
                user == null ? null : user.getEmail(),
                trainer.getPhone(),
                trainer.getAge(),
                trainer.getSpecialty());
    }

    private String effectiveName(Trainer trainer) {
        String trainerName = trainer.getName();
        User user = trainer.getUser();
        String userDisplayName = user == null ? null : user.getDisplayName();
        if (trainerName != null && !trainerName.isBlank()
                && !trainerName.matches(".*_\\d{6,}$")) {
            return trainerName;
        }
        if (userDisplayName != null && !userDisplayName.isBlank()) {
            return userDisplayName;
        }
        if (trainerName != null && !trainerName.isBlank()) {
            return trainerName;
        }
        return user == null ? "Trainer" : user.getUsername();
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

