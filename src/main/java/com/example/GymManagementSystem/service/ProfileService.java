package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.dto.ProfileResponse;
import com.example.GymManagementSystem.dto.ProfileUpdateRequest;
import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.MemberRepository;
import com.example.GymManagementSystem.repository.TrainerRepository;
import com.example.GymManagementSystem.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Base64;
import java.util.Set;

@Service
public class ProfileService {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final TrainerRepository trainerRepository;

    public ProfileService(UserRepository userRepository,
                          MemberRepository memberRepository,
                          TrainerRepository trainerRepository) {
        this.userRepository = userRepository;
        this.memberRepository = memberRepository;
        this.trainerRepository = trainerRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(User user) {
        User current = findUser(user);
        return toResponse(current);
    }

    @Transactional
    public ProfileResponse updateProfile(User user, ProfileUpdateRequest request) {
        User current = findUser(user);
        String displayName = clean(request.getDisplayName());
        String fullName = clean(request.getFullName());
        String phone = clean(request.getPhone());
        String specialty = clean(request.getSpecialty());

        if (displayName != null) {
            requireLength(displayName, 120, "Display name");
            current.setDisplayName(displayName);
        }
        if (request.getAge() != null && (request.getAge() < 1 || request.getAge() > 120)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Age must be between 1 and 120");
        }
        if (phone != null && !phone.matches("^\\+?[0-9\\s()\\-]{7,20}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter a valid phone number");
        }
        if (fullName != null) {
            requireLength(fullName, 120, "Full name");
        }
        if (phone != null) {
            requireLength(phone, 30, "Phone number");
        }
        if (specialty != null) {
            requireLength(specialty, 120, "Specialty");
        }

        if (phone != null) current.setPhone(phone);
        if (request.getAge() != null) current.setAge(request.getAge());

        String role = current.getRole() == null ? "" : current.getRole().toUpperCase();
        if ("MEMBER".equals(role)) {
            Member member = memberRepository.findByUserId(current.getId());
            if (member != null) {
                if (fullName != null) member.setName(fullName);
                if (phone != null) member.setPhone(phone);
                if (request.getAge() != null) member.setAge(request.getAge());
                memberRepository.save(member);
            }
        } else if ("TRAINER".equals(role)) {
            Trainer trainer = trainerRepository.findByUser_Id(current.getId());
            if (trainer != null) {
                if (fullName != null) trainer.setName(fullName);
                if (phone != null) trainer.setPhone(phone);
                if (specialty != null) trainer.setSpecialty(specialty);
                if (request.getAge() != null) trainer.setAge(request.getAge());
                trainerRepository.save(trainer);
            }
        } else if (fullName != null && displayName == null) {
            current.setDisplayName(fullName);
        }

        return toResponse(userRepository.saveAndFlush(current));
    }

    @Transactional
    public ProfileResponse uploadPhoto(User user, MultipartFile file) {
        User current = findUser(user);
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please choose an image");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile photo must be 5 MB or smaller");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only JPG, JPEG, PNG and WEBP images are allowed");
        }
        try {
            String encoded = Base64.getEncoder().encodeToString(file.getBytes());
            current.setProfileImageUrl("data:" + contentType + ";base64," + encoded);
            return toResponse(userRepository.saveAndFlush(current));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read the uploaded image");
        }
    }

    @Transactional
    public ProfileResponse removePhoto(User user) {
        User current = findUser(user);
        current.setProfileImageUrl(null);
        return toResponse(userRepository.saveAndFlush(current));
    }

    private User findUser(User authenticatedUser) {
        return userRepository.findById(authenticatedUser.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));
    }

    private ProfileResponse toResponse(User user) {
        ProfileResponse response = new ProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setDisplayName(user.getDisplayName());
        response.setPhone(user.getPhone());
        response.setAge(user.getAge());
        response.setProfileImageUrl(user.getProfileImageUrl());

        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if ("MEMBER".equals(role)) {
            Member member = memberRepository.findByUserId(user.getId());
            if (member != null) {
                response.setFullName(member.getName());
                response.setPhone(member.getPhone());
                response.setAge(member.getAge());
                response.setMembershipType(member.getMembershipType());
                response.setMembershipStartDate(member.getMembershipStartDate());
                response.setMembershipEndDate(member.getMembershipEndDate());
            }
        } else if ("TRAINER".equals(role)) {
            Trainer trainer = trainerRepository.findByUser_Id(user.getId());
            if (trainer != null) {
                response.setFullName(trainer.getName());
                response.setPhone(trainer.getPhone());
                response.setAge(trainer.getAge());
                response.setSpecialty(trainer.getSpecialty());
            }
        } else {
            response.setFullName(user.getDisplayName());
        }
        if (response.getFullName() == null || response.getFullName().isBlank()) {
            response.setFullName(user.getDisplayName());
        }
        return response;
    }

    private String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void requireLength(String value, int max, String field) {
        if (value.length() > max) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is too long");
        }
    }
}
