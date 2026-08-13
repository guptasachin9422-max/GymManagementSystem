package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.Trainer;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.repository.MemberRepository;
import com.example.GymManagementSystem.repository.TrainerRepository;
import com.example.GymManagementSystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TrainerRepository trainerRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    public Member getMemberById(Integer id) {
        return memberRepository.findById(id).orElse(null);
    }

  public Member updateMember(Integer id, Member member) {

    Member existingMember = memberRepository.findById(id).orElse(null);

    if (existingMember != null) {

        existingMember.setName(member.getName());
        existingMember.setAge(member.getAge());
        existingMember.setPhone(member.getPhone());
        existingMember.setMembershipType(member.getMembershipType());

        // ADD THESE
        existingMember.setMembershipStartDate(member.getMembershipStartDate());
        existingMember.setMembershipEndDate(member.getMembershipEndDate());

        // Fetch trainer from database if provided
        if (member.getTrainer() != null && member.getTrainer().getTrainerId() != null) {
            Trainer trainer = trainerRepository.findById(member.getTrainer().getTrainerId()).orElse(null);
            existingMember.setTrainer(trainer);
        } else {
            existingMember.setTrainer(null);
        }

        // Fetch user from database if provided
        if (member.getUser() != null && member.getUser().getId() != null) {
            User user = userRepository.findById(member.getUser().getId()).orElse(null);
            existingMember.setUser(user);
        } else {
            existingMember.setUser(null);
        }

        return memberRepository.save(existingMember);
    }

    return null;
}
public Member saveMember(Member member) {

    // Fetch trainer from database if trainer ID is provided
    if (member.getTrainer() != null && member.getTrainer().getTrainerId() != null) {
        Trainer trainer = trainerRepository.findById(member.getTrainer().getTrainerId()).orElse(null);
        member.setTrainer(trainer);
    }

    // Fetch user from database if user ID is provided
    if (member.getUser() != null && member.getUser().getId() != null) {
        User user = userRepository.findById(member.getUser().getId()).orElse(null);
        member.setUser(user);
    }

    return memberRepository.save(member);
}

    public String deleteMember(Integer id) {

        memberRepository.deleteById(id);

        return "Member Deleted Successfully";
    }
    

    public List<Member> getMembersByName(String name) {
        return memberRepository.findByName(name);
    }

    public List<Member> getMembersByMembership(String type) {
        return memberRepository.findByMembershipType(type);
    }

    public List<Member> getMembersByAge(Integer age) {
        return memberRepository.findByAge(age);
    }

    public List<Member> getMembersByTrainer(Long trainerId) {
        return memberRepository.findByTrainerTrainerId(trainerId);
    }

    public Member getMyProfile(Integer userId) {
        return memberRepository.findByUserId(userId);
    }
}