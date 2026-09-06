package com.example.GymManagementSystem.controller;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.entity.User;
import com.example.GymManagementSystem.dto.MemberProfileResponse;
import com.example.GymManagementSystem.service.MemberService;
import com.example.GymManagementSystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


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
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return memberService.saveMember(member);
    }

    // ==========================
    // Get All Members
    // ==========================
    @GetMapping("/plans")
    public Object getMembershipPlans() {
        return memberService.getMembershipPlans();
    }

    @GetMapping
    public Object getAllMembers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        return memberService.getAllMembers();
    }

    // ==========================
    // Get Member By Id
    // ==========================
    @GetMapping("/{id:\\d+}")
    public Object getMemberById(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        return memberService.getMemberById(id);
    }

    @GetMapping("/my-members")
    public Object myMembers(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = userService.authenticate(authHeader);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }
        if (!user.getRole().equalsIgnoreCase("TRAINER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        return memberService.getMyTrainerMembers(user.getId());
    }

    // ==========================
    // Update Member
    // ==========================
    @PutMapping("/{id:\\d+}")
    public Object updateMember(
            @PathVariable Integer id,
            @RequestBody Member member,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return memberService.updateMember(id, member);
    }

    // ==========================
    // Delete Member
    // ==========================
    @DeleteMapping("/{id:\\d+}")
    public Object deleteMember(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        if (!user.getRole().equalsIgnoreCase("OWNER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        return memberService.deleteMember(id);
    }

    // ==========================
    // My Profile
    // ==========================
    @GetMapping("/my-profile")
    public Object myProfile(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = userService.authenticate(authHeader);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired session");
        }

        MemberProfileResponse profile = memberService.getMyProfileResponse(user.getId());
        return profile;
    }
}



