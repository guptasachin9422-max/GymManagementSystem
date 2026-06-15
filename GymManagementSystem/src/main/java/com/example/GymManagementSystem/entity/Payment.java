//package com.example.GymManagementSystem.entity;
//
//import jakarta.persistence.*;
//
//@Entity
//@Table(name = "payments")
//public class Payment {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long paymentId;
//
//    private Double amount;
//
//    private String paymentDate;
//
//    private String paymentMethod;
//
//    @ManyToOne
//    @JoinColumn(name = "member_id")
//    private Member member;
//
//    public Payment() {
//    }
//
//    public Long getPaymentId() {
//        return paymentId;
//    }
//
//    public void setPaymentId(Long paymentId) {
//        this.paymentId = paymentId;
//    }
//
//    public Double getAmount() {
//        return amount;
//    }
//
//    public void setAmount(Double amount) {
//        this.amount = amount;
//    }
//
//    public String getPaymentDate() {
//        return paymentDate;
//    }
//
//    public void setPaymentDate(String paymentDate) {
//        this.paymentDate = paymentDate;
//    }
//
//    public String getPaymentMethod() {
//        return paymentMethod;
//    }
//
//    public void setPaymentMethod(String paymentMethod) {
//        this.paymentMethod = paymentMethod;
//    }
//
//    public Member getMember() {
//        return member;
//    }
//
//    public void setMember(Member member) {
//        this.member = member;
//    }
//}