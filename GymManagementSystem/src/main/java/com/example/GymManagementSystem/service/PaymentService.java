//package com.example.GymManagementSystem.service;
//
//import com.example.GymManagementSystem.entity.Member;
//import com.example.GymManagementSystem.entity.Payment;
//import com.example.GymManagementSystem.repository.MemberRepository;
//import com.example.GymManagementSystem.repository.PaymentRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class PaymentService {
//
//    @Autowired
//    private PaymentRepository paymentRepository;
//
//    @Autowired
//    private MemberRepository memberRepository;
//
//    public Payment addPayment(Long memberId, Payment payment) {
//
//        Member member = memberRepository.findById(Math.toIntExact(memberId))
//                .orElseThrow(() -> new RuntimeException("Member Not Found"));
//
//        payment.setMember(member);
//
//        return paymentRepository.save(payment);
//    }
//
//    public List<Payment> getAllPayments() {
//        return paymentRepository.findAll();
//    }
//
//    public Payment getPaymentById(Long id) {
//        return paymentRepository.findById(id).orElse(null);
//    }
//
//    public String deletePayment(Long id) {
//        paymentRepository.deleteById(id);
//        return "Payment Deleted";
//    }
//}