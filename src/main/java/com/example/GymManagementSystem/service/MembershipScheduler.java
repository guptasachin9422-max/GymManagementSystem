package com.example.GymManagementSystem.service;

import com.example.GymManagementSystem.entity.Member;
import com.example.GymManagementSystem.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class MembershipScheduler {

    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private EmailService emailService;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Run daily at 9 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void checkMembershipExpiry() {
        System.out.println("Checking membership expiry reminders at: " + LocalDate.now());
        
        List<Member> allMembers = memberRepository.findAll();
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(7); // 7 days from today
        
        int remindersSent = 0;
        
        for (Member member : allMembers) {
            if (member.getMembershipEndDate() != null && member.getUser() != null) {
                try {
                    LocalDate expiryDate = LocalDate.parse(member.getMembershipEndDate(), dateFormatter);
                    
                    // Check if expiry is exactly 7 days from today
                    if (expiryDate.equals(reminderDate)) {
                        String memberEmail = member.getUser().getEmail();
                        String memberName = member.getName();
                        String formattedExpiryDate = expiryDate.format(dateFormatter);
                        
                        // Send reminder email
                        emailService.sendMembershipExpiryReminder(memberEmail, memberName, formattedExpiryDate);
                        remindersSent++;
                        
                        System.out.println("Sent expiry reminder to: " + memberEmail + 
                                          " for expiry on: " + formattedExpiryDate);
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid date format for member ID " + member.getId() + 
                                      ": " + member.getMembershipEndDate());
                } catch (Exception e) {
                    System.err.println("Failed to send reminder to member ID " + member.getId() + 
                                      ": " + e.getMessage());
                }
            }
        }
        
        System.out.println("Membership expiry check completed. Sent " + remindersSent + " reminders.");
    }
    
    // Run monthly to update membership status
    @Scheduled(cron = "0 0 1 1 * *") // 1st day of every month at midnight
    public void updateExpiredMemberships() {
        System.out.println("Updating expired memberships at: " + LocalDate.now());
        
        List<Member> allMembers = memberRepository.findAll();
        LocalDate today = LocalDate.now();
        
        int expiredCount = 0;
        
        for (Member member : allMembers) {
            if (member.getMembershipEndDate() != null) {
                try {
                    LocalDate expiryDate = LocalDate.parse(member.getMembershipEndDate(), dateFormatter);
                    
                    // Check if membership has expired
                    if (expiryDate.isBefore(today)) {
                        System.out.println("Membership expired for member ID: " + member.getId() + 
                                          ", Name: " + member.getName());
                        expiredCount++;
                        
                        // In a real system, you might want to update member status here
                        // For example: member.setMembershipStatus("Expired");
                        // memberRepository.save(member);
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid date format for member ID " + member.getId());
                }
            }
        }
        
        System.out.println("Membership update completed. Found " + expiredCount + " expired memberships.");
    }
}