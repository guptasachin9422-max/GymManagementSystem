package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.dto.AiChatRequest;
import com.example.GymManagementSystem.dto.AiChatResponse;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.AiService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;
    private final UserService userService;

    public AiController(AiService aiService, UserService userService) {
        this.aiService = aiService;
        this.userService = userService;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(
            @RequestBody AiChatRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = userService.authenticate(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }
        if (request == null || request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body("Message is required");
        }
        try {
            return ResponseEntity.ok(new AiChatResponse(aiService.chat(user, request.message())));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(exception.getMessage());
        }
    }
}
