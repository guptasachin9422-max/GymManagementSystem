package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.service.MemberService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

    // ==========================
    // Add Member (OWNER)
    // ==========================
    @PostMapping
    public Object addMember(
            @RequestBody Member member,
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return "Access Denied";
        }

        return memberService.saveMember(member);
    }

    // ==========================
    // Get All Members
    // ==========================
    @GetMapping
    public Object getAllMembers(
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        return memberService.getAllMembers();
    }

    // ==========================
    // Get Member By Id
    // ==========================
    @GetMapping("/{id}")
    public Object getMemberById(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        return memberService.getMemberById(id);
    }

    // ==========================
    // Update Member
    // ==========================
    @PutMapping("/{id}")
    public Object updateMember(
            @PathVariable Integer id,
            @RequestBody Member member,
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return "Access Denied";
        }

        return memberService.updateMember(id, member);
    }

    // ==========================
    // Delete Member
    // ==========================
    @DeleteMapping("/{id}")
    public Object deleteMember(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return "Access Denied";
        }

        return memberService.deleteMember(id);
    }

    // ==========================
    // My Profile
    // ==========================
    @GetMapping("/my-profile")
    public Object myProfile(
            @RequestHeader("Authorization") String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return "Invalid Token";
        }

        return memberService.getMyProfile(user.getId());
    }
}