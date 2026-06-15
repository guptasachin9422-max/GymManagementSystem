package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

        @Autowired
        private MemberService memberService;

        @PostMapping
        public Member addMember(@RequestBody Member member) {
                return memberService.saveMember(member);
        }

        @GetMapping
        public List<Member> getAllMembers() {
                return memberService.getAllMembers();
        }


        @GetMapping("/{id}")
        public Member getMemberById(@PathVariable Integer id) {
                return memberService.getMemberById(id);
        }


        @PutMapping("/{id}")
        public Member updateMember(
                @PathVariable Integer id,
                @RequestBody Member member) {

                return memberService.updateMember(id, member);
        }

        @DeleteMapping("/{id}")
        public String deleteMember(@PathVariable Integer id) {
                return memberService.deleteMember(id);
        }
}