package com.example.GymManagementSystem.dto;

public class RazorpayOrderRequest {
    private Double amount;
    private Integer memberId;

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Integer getMemberId() { return memberId; }
    public void setMemberId(Integer memberId) { this.memberId = memberId; }
}
