package com.example.GymManagementSystem.controller;

//import com.example.GymManagementSystem.entity.Payment;
//import com.example.GymManagementSystem.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
//@RequestMapping("/payments")
//public class PaymentController {

//    @Autowired
    //private PaymentService paymentService;

//    @PostMapping("/{memberId}")
//    public Payment addPayment(
//            @PathVariable Long memberId,
//            @RequestBody Payment payment) {
//
//        return paymentService.addPayment(memberId, payment);
//    }

//    @GetMapping
//    public List<Payment> getAllPayments() {
//        return paymentService.getAllPayments();
//    }

//    @GetMapping("/{id}")
//    public Payment getPaymentById(@PathVariable Long id) {
//        return paymentService.getPaymentById(id);
//    }

//    @DeleteMapping("/{id}")
//    public String deletePayment(@PathVariable Long id) {
//        return paymentService.deletePayment(id);
//    }
//}