package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.Payment;
import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.dto.MemberProfileResponse;
import com.example.GymManagementSystem.dto.TrainerMemberResponse;
import com.example.GymManagementSystem.repository.MemberRepository;
import com.example.GymManagementSystem.repository.PaymentRepository;
import com.example.GymManagementSystem.repository.TrainerRepository;
import com.example.GymManagementSystem.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.util.Arrays;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // Get All Members
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }


    // Get Member By ID
    public Member getMemberById(Integer id) {
        return memberRepository.findById(id).orElse(null);
    }


    // Add Member
    @org.springframework.transaction.annotation.Transactional
    public Member saveMember(Member member) {

        // Fetch Trainer from database
        if (member.getTrainer() != null
                && member.getTrainer().getTrainerId() != null) {

            Long trainerId = member.getTrainer().getTrainerId();

            Trainer trainer = trainerRepository.findById(trainerId)
                    .orElseThrow(() ->
                            new RuntimeException("Trainer not found with ID: " + trainerId));

            member.setTrainer(trainer);
        } else {
            member.setTrainer(null);
        }


        // Fetch User from database
        if (member.getUser() != null
                && member.getUser().getId() != null) {

            Integer userId = member.getUser().getId();

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new RuntimeException("User not found with ID: " + userId));

            member.setUser(user);
        } else {
            member.setUser(null);
        }

        Member savedMember = memberRepository.save(member);
        System.out.printf("Member saved: memberId=%d, trainerId=%s, trainerName=%s%n",
                savedMember.getId(),
                savedMember.getTrainerId(),
                savedMember.getTrainerName());

        // A new member gets exactly one reusable payment record. Razorpay order
        // creation updates this record instead of inserting another one.
        Optional<Payment> activePayment =
                paymentRepository.findFirstByMemberIdAndPaymentStatusInOrderByPaymentIdDesc(
                        savedMember.getId(), Arrays.asList("PENDING", "PROCESSING", "CREATED"));
        if (activePayment.isEmpty()) {
            Payment payment = new Payment();
            payment.setAmount(membershipAmount(savedMember.getMembershipType()));
            payment.setPaymentDate(LocalDate.now().toString());
            payment.setPaymentMethod("RAZORPAY");
            payment.setPaymentStatus("PENDING");
            payment.setMember(savedMember);
            paymentRepository.save(payment);
        } else if ("CREATED".equalsIgnoreCase(activePayment.get().getPaymentStatus())) {
            activePayment.get().setPaymentStatus("PENDING");
            paymentRepository.save(activePayment.get());
        }

        return savedMember;
    }

    private double membershipAmount(String membershipType) {
        return switch (membershipType) {
            case "Basic" -> 1200d;
            case "Premium" -> 2500d;
            case "VIP" -> 6000d;
            default -> throw new IllegalArgumentException("Invalid membership plan");
        };
    }


    // Update Member
    public Member updateMember(Integer id, Member member) {

        Member existingMember = memberRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Member not found with ID: " + id));

        existingMember.setName(member.getName());
        existingMember.setAge(member.getAge());
        existingMember.setPhone(member.getPhone());
        existingMember.setMembershipType(member.getMembershipType());
        existingMember.setMembershipStartDate(member.getMembershipStartDate());
        existingMember.setMembershipEndDate(member.getMembershipEndDate());


        // Update Trainer
        if (member.getTrainer() != null
                && member.getTrainer().getTrainerId() != null) {

            Long trainerId = member.getTrainer().getTrainerId();

            Trainer trainer = trainerRepository.findById(trainerId)
                    .orElseThrow(() ->
                            new RuntimeException("Trainer not found with ID: " + trainerId));

            existingMember.setTrainer(trainer);
        } else {
            existingMember.setTrainer(null);
        }


        // Update User
        if (member.getUser() != null
                && member.getUser().getId() != null) {

            Integer userId = member.getUser().getId();

            User user = userRepository.findById(userId)
                    .orElseThrow(() ->
                            new RuntimeException("User not found with ID: " + userId));

            existingMember.setUser(user);
        }

        Member savedMember = memberRepository.save(existingMember);
        System.out.printf("Member updated: memberId=%d, trainerId=%s, trainerName=%s%n",
                savedMember.getId(),
                savedMember.getTrainerId(),
                savedMember.getTrainerName());
        return savedMember;
    }


    // Delete Member
    public String deleteMember(Integer id) {

        if (!memberRepository.existsById(id)) {
            return "Member not found";
        }

        memberRepository.deleteById(id);
        return "Member Deleted Successfully";
    }


    // Search By Name
    public List<Member> getMembersByName(String name) {
        return memberRepository.findByName(name);
    }


    // Search By Membership
    public List<Member> getMembersByMembership(String type) {
        return memberRepository.findByMembershipType(type);
    }


    // Search By Age
    public List<Member> getMembersByAge(Integer age) {
        return memberRepository.findByAge(age);
    }


    // Get Members By Trainer
    public List<Member> getMembersByTrainer(Long trainerId) {
        return memberRepository.findByTrainerTrainerId(trainerId);
    }


    // Get My Profile
    public Member getMyProfile(Integer userId) {
        return memberRepository.findByUserId(userId);
    }

    public MemberProfileResponse getMyProfileResponse(Integer userId) {
        Member member = memberRepository.findByUserId(userId);
        return member == null ? null : MemberProfileResponse.from(member);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<TrainerMemberResponse> getMyTrainerMembers(Integer userId) {
        Trainer trainer = trainerRepository.findByUser_Id(userId);
        if (trainer == null) {
            return List.of();
        }

        List<TrainerMemberResponse> result = memberRepository
                .findByTrainerTrainerId(trainer.getTrainerId())
                .stream()
                .map(member -> {
                    String email = member.getUser() == null ? null : member.getUser().getEmail();
                    String paymentStatus = paymentRepository
                            .findFirstByMemberIdOrderByPaymentIdDesc(member.getId())
                            .map(Payment::getPaymentStatus)
                            .orElse("PENDING");
                    return new TrainerMemberResponse(
                            member.getId(), member.getName(), email, member.getPhone(),
                            member.getAge(), member.getMembershipType(),
                            member.getMembershipStartDate(), member.getMembershipEndDate(),
                            paymentStatus);
                })
                .toList();
        System.out.printf("Trainer members: userId=%d, trainerId=%d, count=%d%n",
                userId, trainer.getTrainerId(), result.size());
        return result;
    }
}
