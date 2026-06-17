package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.TrainerRepository;
import com.example.GymManagementSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.repository.MemberRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TrainerRepository trainerRepository;

    public ResponseEntity<User> register(User user) {
        return ResponseEntity.ok(userRepository.save(user));
    }

    public ResponseEntity<?> login(User user) {

        User dbUser =
                userRepository.findByUsername(user.getUsername());

        if (dbUser == null) {
            return ResponseEntity.status(404).body("User Not Found");
        }

        if (!dbUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(401).body("Wrong Password");
        }
        if ("OWNER".equalsIgnoreCase(dbUser.getRole())) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Owner Login Successful");
            response.put("members", memberRepository.findAll());
            response.put("trainers", getTrainersWithoutMembers());
            return ResponseEntity.ok(response);
        }

        if ("Trainer".equalsIgnoreCase(dbUser.getRole())) {
            Trainer trainer = trainerRepository.findByUserId(dbUser.getId());

            if (trainer == null) {
                return ResponseEntity.status(404).body("Trainer Profile Not Found");
            }

            return ResponseEntity.ok(trainer);
        }

        Member member = memberRepository.findByUserId(dbUser.getId());

        if (member == null) {
            return ResponseEntity.ok("Login Successful. Please create your Member Profile.");
        }

        return ResponseEntity.ok(member);
    }

    public ResponseEntity<?> getAllUsers(int userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User Not Found");
        }

        if ("OWNER".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.ok(userRepository.findAll());
        }

        return ResponseEntity.status(403).body("Access Denied!");
    }

    public ResponseEntity<?> getAllTrainers(int userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User Not Found");
        }

        if ("OWNER".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.ok(getTrainersWithoutMembers());
        }

        return ResponseEntity.status(403).body("Access Denied!");
    }

    private List<Map<String, Object>> getTrainersWithoutMembers() {
        return trainerRepository.findAll()
                .stream()
                .map(trainer -> {
                    Map<String, Object> trainerResponse = new HashMap<>();
                    trainerResponse.put("trainerId", trainer.getTrainerId());
                    trainerResponse.put("name", trainer.getName());
                    trainerResponse.put("specialty", trainer.getSpecialty());
                    trainerResponse.put("phone", trainer.getPhone());
                    trainerResponse.put("user", trainer.getUser());
                    return trainerResponse;
                })
                .toList();
    }

    public ResponseEntity<?> getUserById(int id, int userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User Not Found");
        }

        if (!"OWNER".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403).body("Access Denied!");
        }

        User requestedUser = userRepository.findById(id).orElse(null);

        if (requestedUser == null) {
            return ResponseEntity.status(404).body("Requested User Not Found");
        }

        return ResponseEntity.ok(requestedUser);
    }

    public ResponseEntity<String> deleteUser(int id, int userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body("User Not Found");
        }

        if (!"OWNER".equalsIgnoreCase(user.getRole())) {
            return ResponseEntity.status(403).body("Access Denied!");
        }

        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Requested User Not Found");
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok("User Deleted Successfully");
    }
}
