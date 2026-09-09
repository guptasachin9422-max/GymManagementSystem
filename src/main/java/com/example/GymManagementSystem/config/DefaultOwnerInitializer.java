package com.example.GymManagementSystem.config;

import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DefaultOwnerInitializer {

    private static final String OWNER_USERNAME = "owner";
    private static final String OWNER_EMAIL = "owner@fitlife.com";

    @Bean
    CommandLineRunner createDefaultOwner(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            boolean ownerExists = userRepository.findByEmail(OWNER_EMAIL).isPresent()
                    || userRepository.findByUsername(OWNER_USERNAME) != null;

            if (ownerExists) {
                return;
            }

            User owner = new User();
            owner.setDisplayName("Gym Owner");
            owner.setUsername(OWNER_USERNAME);
            owner.setEmail(OWNER_EMAIL);
            owner.setPassword(passwordEncoder.encode("Owner@123"));
            owner.setRole("OWNER");
            userRepository.save(owner);
        };
    }
}
