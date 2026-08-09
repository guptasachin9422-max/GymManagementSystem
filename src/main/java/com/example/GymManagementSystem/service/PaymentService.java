package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.Payment;
import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.GymManagementSystem.repository.MemberRepository;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {
    @Autowired
private MemberRepository memberRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private EmailService emailService;

    // Add Payment
    public Payment savePayment(Payment payment) {
        Payment savedPayment = paymentRepository.save(payment);
        
        // Send payment success email if payment status is successful
        if ("Completed".equalsIgnoreCase(payment.getPaymentStatus()) || 
            "Success".equalsIgnoreCase(payment.getPaymentStatus())) {
            sendPaymentSuccessEmail(savedPayment);
        }
        
        return savedPayment;
    }
    
    private void sendPaymentSuccessEmail(Payment payment) {

    try {

        Member member = memberRepository
                .findById(payment.getMember().getId())
                .orElse(null);

        if (member == null) {
            System.out.println("Member not found");
            return;
        }

        if (member.getUser() == null) {
            System.out.println("User not linked with member");
            return;
        }

        String memberEmail = member.getUser().getEmail();

        String transactionId =
                "TRX-" + payment.getPaymentId();

        emailService.sendPaymentSuccessEmail(

                memberEmail,

                member.getName(),

                payment.getAmount(),

                payment.getPaymentDate().toString(),

                payment.getPaymentMethod(),

                transactionId

        );

        System.out.println("Payment Email Sent");

    }

    catch (Exception e) {

        e.printStackTrace();

    }

}

    // Get All Payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Get Payment By Id
    public Payment getPaymentById(Integer id) {
        return paymentRepository.findById(id).orElse(null);
    }

    // Update Payment
    public Payment updatePayment(Integer id, Payment payment) {

        Payment existing =
                paymentRepository.findById(id).orElse(null);

        if (existing == null) {
            return null;
        }

        existing.setAmount(payment.getAmount());
        existing.setPaymentDate(payment.getPaymentDate());
        existing.setPaymentMethod(payment.getPaymentMethod());
        existing.setPaymentStatus(payment.getPaymentStatus());
        existing.setMember(payment.getMember());

        return paymentRepository.save(existing);
    }

    // Delete
    public String deletePayment(Integer id) {

        paymentRepository.deleteById(id);

        return "Payment Deleted Successfully";
    }

    // Member My Payment
    public List<Payment> getPaymentByUserId(Integer userId) {

        return paymentRepository.findByMemberUserId(userId);
    }

}