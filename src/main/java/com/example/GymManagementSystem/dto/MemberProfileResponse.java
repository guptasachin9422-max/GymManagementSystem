package com.example.GymManagementSystem.dto;

import com.example.GymManagementSystem.entity.Member;

public class MemberProfileResponse {
    private Integer id;
    private String name;
    private Integer age;
    private String phone;
    private String membershipType;
    private String membershipStartDate;
    private String membershipEndDate;
    private Long trainerId;
    private String trainerName;

    public static MemberProfileResponse from(Member member) {
        MemberProfileResponse response = new MemberProfileResponse();
        response.id = member.getId();
        response.name = member.getName();
        response.age = member.getAge();
        response.phone = member.getPhone();
        response.membershipType = member.getMembershipType();
        response.membershipStartDate = member.getMembershipStartDate();
        response.membershipEndDate = member.getMembershipEndDate();
        if (member.getTrainer() != null) {
            response.trainerId = member.getTrainer().getTrainerId();
            String trainerName = member.getTrainer().getName();
            String displayName = member.getTrainer().getUser() == null
                    ? null : member.getTrainer().getUser().getDisplayName();
            response.trainerName = trainerName != null && !trainerName.isBlank()
                    && !trainerName.matches(".*_\\d{6,}$")
                    ? trainerName
                    : (displayName != null && !displayName.isBlank()
                    ? displayName : trainerName);
        }
        return response;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public Integer getAge() { return age; }
    public String getPhone() { return phone; }
    public String getMembershipType() { return membershipType; }
    public String getMembershipStartDate() { return membershipStartDate; }
    public String getMembershipEndDate() { return membershipEndDate; }
    public Long getTrainerId() { return trainerId; }
    public String getTrainerName() { return trainerName; }
}
