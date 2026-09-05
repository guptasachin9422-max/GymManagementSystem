package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.dto.UserManagementResponse;
import com.example.GymManagementSystem.repository.MemberRepository;
import com.example.GymManagementSystem.repository.TrainerRepository;
import com.example.GymManagementSystem.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private LogoutService logoutService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    // ===========================
    // Register
    // ===========================
    @Transactional
    public ResponseEntity<?> register(User user) {

        User existingUsername =
                userRepository.findByUsername(user.getUsername());

        if (existingUsername != null) {
            return ResponseEntity.badRequest()
                    .body("Username Already Exists");
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body("Email Already Exists");
        }

        User savedUser = userRepository.save(user);

        if ("TRAINER".equalsIgnoreCase(savedUser.getRole())
                && trainerRepository.findByUser_Id(savedUser.getId()) == null) {
            Trainer trainer = new Trainer();
            String name = firstNonBlank(savedUser.getTrainerName(), savedUser.getDisplayName(),
                    savedUser.getUsername());
            trainer.setName(name);
            trainer.setSpecialty(savedUser.getTrainerSpecialty());
            trainer.setPhone(savedUser.getTrainerPhone());
            trainer.setAge(savedUser.getTrainerAge());
            trainer.setUser(savedUser);
            trainerRepository.save(trainer);
            System.out.printf("Trainer created: userId=%d, trainerId=%d, name=%s%n",
                    savedUser.getId(), trainer.getTrainerId(), trainer.getName());
        }

        try {
            emailService.sendWelcomeEmail(
                    user.getEmail(),
                    user.getUsername()
            );
        } catch (Exception e) {
            System.err.println(
                    "Failed to send welcome email: "
                            + e.getMessage()
            );
        }

        return ResponseEntity.ok(savedUser);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value.trim();
        }
        return "Trainer";
    }


    // ===========================
    // Login
    // ===========================
    public ResponseEntity<?> login(User user) {

        User dbUser = userRepository.findByEmail(user.getEmail())
                .orElse(null);

        if (dbUser == null) {
            return ResponseEntity.badRequest()
                    .body("Email Not Found");
        }

        if (!dbUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.badRequest()
                    .body("Wrong Password");
        }

        // Generate JWT Token
        String token = jwtService.generateToken(dbUser.getUsername());

        // Response
        Map<String, Object> response = new HashMap<>();

        response.put("message", "Login Successful");
        response.put("token", token);
        response.put("username", dbUser.getUsername());
        response.put("displayName", displayNameFor(dbUser));
        response.put("email", dbUser.getEmail());
        response.put("role", dbUser.getRole());

        return ResponseEntity.ok(response);
    }


    // ===========================
    // Authenticate Token
    // ===========================
    public User authenticate(String authHeader) {

        if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {
            return null;
        }

        String token = authHeader.substring(7).trim();

        // Check if token is logged out / blacklisted
        if (logoutService.isTokenBlacklisted(token)) {
            return null;
        }

        try {
            String username = jwtService.extractUsername(token);

            if (username == null) {
                return null;
            }

            User user = userRepository.findByUsername(username);

            if (user == null) {
                return null;
            }

            // Validate JWT token only
            if (!jwtService.validateToken(token, username)) {
                return null;
            }

            return user;

        } catch (Exception e) {
            return null;
        }
    }


    // ===========================
    // Get All Users
    // ===========================
    public List<UserManagementResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserManagementResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        displayNameFor(user)))
                .collect(Collectors.toList());
    }

    private String displayNameFor(User user) {
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if ("MEMBER".equals(role)) {
            Member member = memberRepository.findByUserId(user.getId());
            if (member != null && member.getName() != null && !member.getName().isBlank()) {
                return member.getName();
            }
        } else if ("TRAINER".equals(role)) {
            Trainer trainer = trainerRepository.findByUser_Id(user.getId());
            if (trainer != null && trainer.getName() != null && !trainer.getName().isBlank()
                    && !looksGeneratedUsername(trainer.getName())) {
                return trainer.getName();
            }
        } else if (user.getDisplayName() != null && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }
        if ("TRAINER".equals(role) && user.getDisplayName() != null
                && !user.getDisplayName().isBlank()) {
            return user.getDisplayName();
        }
        return user.getUsername();
    }

    private boolean looksGeneratedUsername(String value) {
        return value.matches(".*_\\d{6,}$");
    }

    public UserManagementResponse updateDisplayName(Integer id, String displayName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setDisplayName(displayName == null ? null : displayName.trim());
        User saved = userRepository.save(user);
        return new UserManagementResponse(saved.getId(), saved.getUsername(), saved.getEmail(),
                saved.getRole(), displayNameFor(saved));
    }


    // ===========================
    // Get User By Id
    // ===========================
    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElse(null);
    }


    // ===========================
    // Delete User
    // ===========================
    public String deleteUser(Integer id) {
        userRepository.deleteById(id);
        return "User Deleted Successfully";
    }
}

