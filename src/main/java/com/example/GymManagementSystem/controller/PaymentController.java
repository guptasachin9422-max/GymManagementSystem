package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.Payment;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.PaymentService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;

    // Add Payment
    @PostMapping
    public Object addPayment(
            @RequestBody Payment payment,
            @RequestHeader("Authorization") String authHeader) {

        User user =
                userService.authenticate(authHeader);

        if (user == null)
            return "Invalid Token";

        if (!user.getRole().equalsIgnoreCase("OWNER"))
            return "Access Denied";

        return paymentService.savePayment(payment);
    }

    // All Payments
    @GetMapping
    public Object getAllPayments(
            @RequestHeader("Authorization") String authHeader) {

        User user =
                userService.authenticate(authHeader);

        if (user == null)
            return "Invalid Token";

        return paymentService.getAllPayments();
    }

    // Payment By Id
    @GetMapping("/{id}")
    public Object getPaymentById(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {

        User user =
                userService.authenticate(authHeader);

        if (user == null)
            return "Invalid Token";

        return paymentService.getPaymentById(id);
    }

    // Update Payment
    @PutMapping("/{id}")
    public Object updatePayment(
            @PathVariable Integer id,
            @RequestBody Payment payment,
            @RequestHeader("Authorization") String authHeader) {

        User user =
                userService.authenticate(authHeader);

        if (user == null)
            return "Invalid Token";

        if (!user.getRole().equalsIgnoreCase("OWNER"))
            return "Access Denied";

        return paymentService.updatePayment(id, payment);
    }

    // Delete Payment
    @DeleteMapping("/{id}")
    public Object deletePayment(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {

        User user =
                userService.authenticate(authHeader);

        if (user == null)
            return "Invalid Token";

        if (!user.getRole().equalsIgnoreCase("OWNER"))
            return "Access Denied";

        return paymentService.deletePayment(id);
    }

    // My Payment
    @GetMapping("/my-payment")
    public Object myPayment(
            @RequestHeader("Authorization") String authHeader) {

        User user =
                userService.authenticate(authHeader);

        if (user == null)
            return "Invalid Token";

        return paymentService.getPaymentByUserId(user.getId());
    }
}