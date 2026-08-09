package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

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

        existingMember.setTrainer(member.getTrainer());
        existingMember.setUser(member.getUser());

        return memberRepository.save(existingMember);
    }

    return null;
}
public Member saveMember(Member member) {

    if (member.getUser() != null) {
        System.out.println("User ID: " + member.getUser().getId());
    }

    if (member.getTrainer() != null) {
        System.out.println("Trainer ID: " + member.getTrainer().getTrainerId());
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