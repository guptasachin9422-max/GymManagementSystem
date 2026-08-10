package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // ===========================
    // Register
    // ===========================
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

    userRepository.save(user);

    // Send welcome email
    try {
        emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
    } catch (Exception e) {
        System.err.println("Failed to send welcome email: " + e.getMessage());
        // Continue with registration even if email fails
    }

    return ResponseEntity.ok(user);
}

    // ===========================
    // Login
    // ===========================
    public ResponseEntity<?> login(User user) {

    User dbUser =
            userRepository.findByEmail(user.getEmail())
                    .orElse(null);

    if (dbUser == null) {
        return ResponseEntity.badRequest()
                .body("Email Not Found");
    }

    if (!dbUser.getPassword().equals(user.getPassword())) {
        return ResponseEntity.badRequest()
                .body("Wrong Password");
    }

    String token =
            jwtService.generateToken(dbUser.getUsername());

            dbUser.setAccessToken(token);
userRepository.save(dbUser);


    Map<String, Object> response =
            new HashMap<>();

    response.put("message", "Login Successful");
    response.put("token", token);
    response.put("username", dbUser.getUsername());
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

    String token = authHeader.substring(7);

    // Check blacklisted token
    if (logoutService.isTokenBlacklisted(token)) {
        return null;
    }

    String username = jwtService.extractUsername(token);

    User user = userRepository.findByUsername(username);

    if (user == null) {
        return null;
    }

    if (!jwtService.validateToken(token, username)) {
        return null;
    }

    return user;
}

    // ===========================
    // Get All Users
    // ===========================
    public List<User> getAllUsers() {
        return userRepository.findAll();
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