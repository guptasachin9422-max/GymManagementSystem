package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.dto.ProfileUpdateRequest;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.ProfileService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user/profile")
public class ProfileController {

    private final UserService userService;
    private final ProfileService profileService;

    public ProfileController(UserService userService, ProfileService profileService) {
        this.userService = userService;
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authenticated(authHeader);
        if (user == null) return unauthorized();
        return ResponseEntity.ok(profileService.getProfile(user));
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authenticated(authHeader);
        if (user == null) return unauthorized();
        return ResponseEntity.ok(profileService.updateProfile(user, request));
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhoto(
            @RequestPart("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authenticated(authHeader);
        if (user == null) return unauthorized();
        return ResponseEntity.ok(profileService.uploadPhoto(user, file));
    }

    @DeleteMapping("/photo")
    public ResponseEntity<?> deletePhoto(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = authenticated(authHeader);
        if (user == null) return unauthorized();
        return ResponseEntity.ok(profileService.removePhoto(user));
    }

    private User authenticated(String authHeader) {
        return userService.authenticate(authHeader);
    }

    private ResponseEntity<String> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
    }
}
