package com.example.GymManagementSystem.dto;

public class TrainerMemberResponse {
    private Integer memberId;
    private String name;
    private String email;
    private String phone;
    private Integer age;
    private String membershipType;
    private String membershipStartDate;
    private String membershipEndDate;
    private String paymentStatus;

    public TrainerMemberResponse(Integer memberId, String name, String email,
                                 String phone, Integer age, String membershipType,
                                 String membershipStartDate, String membershipEndDate,
                                 String paymentStatus) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.age = age;
        this.membershipType = membershipType;
        this.membershipStartDate = membershipStartDate;
        this.membershipEndDate = membershipEndDate;
        this.paymentStatus = paymentStatus;
    }

    public Integer getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Integer getAge() { return age; }
    public String getMembershipType() { return membershipType; }
    public String getMembershipStartDate() { return membershipStartDate; }
    public String getMembershipEndDate() { return membershipEndDate; }
    public String getPaymentStatus() { return paymentStatus; }
}
