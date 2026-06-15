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


    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }


    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }


    public Member getMemberById(Integer id) {
        return memberRepository.findById(id).orElse(null);
    }


    public Member updateMember(Integer id, Member member) {

        Member existingMember =
                memberRepository.findById(id).orElse(null);

        if (existingMember != null) {

            existingMember.setName(member.getName());
            existingMember.setAge(member.getAge());
            existingMember.setPhone(member.getPhone());
            existingMember.setMembershipType(
                    member.getMembershipType());


            existingMember.setTrainer(
                    member.getTrainer());

            return memberRepository.save(existingMember);
        }

        return null;
    }


    public String deleteMember(Integer id) {
        memberRepository.deleteById(id);
        return "Member Deleted Successfully";
    }
}