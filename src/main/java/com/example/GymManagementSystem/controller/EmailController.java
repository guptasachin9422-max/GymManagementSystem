package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.UserRepository;
import com.example.GymManagementSystem.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    // Email -> OTP
    private final Map<String, String> otpStore = new HashMap<>();

    // ==========================
    // Send OTP
    // ==========================
    @PostMapping("/password-reset/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {

        String email = request.get("email");

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email not found");
        }

        String otp = emailService.generateOTP();

        otpStore.put(email.trim().toLowerCase(), otp);

        System.out.println("================================");
        System.out.println("OTP Generated : " + otp);
        System.out.println("Email         : " + email);
        System.out.println("OTP Store     : " + otpStore);
        System.out.println("================================");

        User user = userOpt.get();

        emailService.sendPasswordResetEmail(
                email,
                user.getUsername(),
                otp
        );

        return ResponseEntity.ok("OTP Sent Successfully");
    }

    // ==========================
    // Verify OTP
    // ==========================
    @PostMapping("/password-reset/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (email == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body("All fields are required");
        }

        email = email.trim().toLowerCase();

        String storedOtp = otpStore.get(email);

        System.out.println("================================");
        System.out.println("Email        : " + email);
        System.out.println("Entered OTP  : " + otp);
        System.out.println("Stored OTP   : " + storedOtp);
        System.out.println("OTP Store    : " + otpStore);
        System.out.println("================================");

        if (storedOtp == null) {
            return ResponseEntity.badRequest().body("OTP not found. Generate OTP again.");
        }

        if (!storedOtp.equals(otp.trim())) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found");
        }

       User user = userOpt.get();

        user.setPassword(newPassword);

        userRepository.save(user);

        // Password Changed Mail
        emailService.sendPasswordChangedEmail(
                user.getEmail(),
                user.getUsername()
        );

        otpStore.remove(email);

        return ResponseEntity.ok("Password Changed Successfully");
    }
    

    // ==========================
    // Contact Us
    // ==========================
    @PostMapping("/contact")
    public ResponseEntity<?> contactUs(@RequestBody Map<String, String> request) {

        emailService.sendContactUsEmail(
                request.get("name"),
                request.get("email"),
                request.get("subject"),
                request.get("message")
        );

        return ResponseEntity.ok("Message Sent Successfully");
    }

}