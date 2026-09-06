package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.dto.RazorpayOrderRequest;
import com.example.GymManagementSystem.dto.RazorpayVerifyRequest;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.RazorpayService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments/razorpay")
public class RazorpayController {
    private final RazorpayService razorpayService;
    private final UserService userService;

    public RazorpayController(RazorpayService razorpayService, UserService userService) {
        this.razorpayService = razorpayService;
        this.userService = userService;
    }

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@RequestBody RazorpayOrderRequest request,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = userService.authenticate(authHeader);
        if (user == null) return ResponseEntity.status(401).body("Invalid or expired session");
        try {
            boolean owner = "OWNER".equalsIgnoreCase(user.getRole());
            return ResponseEntity.ok(razorpayService.createOrder(request, user.getId(), owner));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody RazorpayVerifyRequest request,
                                            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = userService.authenticate(authHeader);
        if (user == null) return ResponseEntity.status(401).body("Invalid or expired session");
        try {
            return ResponseEntity.ok(razorpayService.verifyPayment(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Payment verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/failed")
    public ResponseEntity<?> paymentFailed(@RequestBody RazorpayVerifyRequest request,
                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = userService.authenticate(authHeader);
        if (user == null) return ResponseEntity.status(401).body("Invalid or expired session");
        try {
            return ResponseEntity.ok(razorpayService.markPaymentFailed(request.getOrderId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Payment update failed: " + e.getMessage());
        }
    }
}


