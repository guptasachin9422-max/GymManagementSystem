package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.dto.RazorpayOrderRequest;
import com.example.GymManagementSystem.dto.RazorpayVerifyRequest;
import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.MembershipPlan;
import com.example.GymManagementSystem.entity.Payment;
import com.example.GymManagementSystem.repository.MemberRepository;
import com.example.GymManagementSystem.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class RazorpayService {
    @Value("${razorpay.key.id}") private String keyId;
    @Value("${razorpay.key.secret}") private String keySecret;

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    public RazorpayService(MemberRepository memberRepository, PaymentRepository paymentRepository) {
        this.memberRepository = memberRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public synchronized Map<String, Object> createOrder(
            RazorpayOrderRequest request, Integer userId, boolean owner) throws Exception {
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new IllegalStateException("Razorpay keys are not configured");
        }

        Member member;
        if (request.getMemberId() != null && owner) {
            member = memberRepository.findById(request.getMemberId()).orElse(null);
        } else {
            member = memberRepository.findByUserId(userId);
        }
        if (member == null) throw new IllegalArgumentException("Member not found");

        MembershipPlan plan = MembershipPlan.from(member.getMembershipType());
        double fixedAmount = plan.getPrice();
        int amountInPaise = (int) Math.round(fixedAmount * 100);

        Optional<Payment> existingPayment =
                paymentRepository.findFirstByMemberIdAndPaymentStatusInOrderByPaymentIdDesc(
                        member.getId(), Arrays.asList("PENDING", "PROCESSING", "CREATED"));
        if (existingPayment.isPresent()
                && existingPayment.get().getRazorpayOrderId() != null
                && !existingPayment.get().getRazorpayOrderId().isBlank()) {
            Payment payment = existingPayment.get();
            payment.setPaymentStatus("PROCESSING");
            paymentRepository.save(payment);

            Map<String, Object> response = new HashMap<>();
            response.put("keyId", keyId);
            response.put("orderId", payment.getRazorpayOrderId());
            response.put("amount", amountInPaise);
            response.put("currency", "INR");
            return response;
        }

        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        JSONObject options = new JSONObject();
        options.put("amount", amountInPaise);
        options.put("currency", "INR");
        options.put("receipt", "gym_" + System.currentTimeMillis());
        Order order = client.orders.create(options);
        String orderId = order.get("id");

        Payment payment = existingPayment.orElseGet(Payment::new);
        payment.setAmount(fixedAmount);
        payment.setPaymentDate(LocalDate.now().toString());
        payment.setPaymentMethod("RAZORPAY");
        payment.setPaymentStatus("PROCESSING");
        payment.setRazorpayOrderId(orderId);
        payment.setMember(member);
        paymentRepository.save(payment);

        Map<String, Object> response = new HashMap<>();
        response.put("keyId", keyId);
        response.put("orderId", orderId);
        response.put("amount", amountInPaise);
        response.put("currency", "INR");
        return response;
    }

    @Transactional
    public Payment verifyPayment(RazorpayVerifyRequest request) throws Exception {
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getOrderId()).orElse(null);
        if (payment == null) throw new IllegalArgumentException("Payment order not found");

        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", request.getOrderId());
        attributes.put("razorpay_payment_id", request.getPaymentId());
        attributes.put("razorpay_signature", request.getSignature());
        Utils.verifyPaymentSignature(attributes, keySecret);

        payment.setRazorpayPaymentId(request.getPaymentId());
        payment.setRazorpaySignature(request.getSignature());
        payment.setPaymentStatus("PAID");
        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment markPaymentFailed(String orderId) {
        Payment payment = paymentRepository.findByRazorpayOrderId(orderId).orElse(null);
        if (payment == null) throw new IllegalArgumentException("Payment order not found");
        payment.setPaymentStatus("FAILED");
        return paymentRepository.save(payment);
    }
}
