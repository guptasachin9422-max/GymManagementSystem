package com.example.GymManagementSystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDate;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${app.email.from:noreply@gymmanagement.com}")
    private String fromEmail;
    
    @Value("${app.gym.name:FitLife Gym}")
    private String gymName;
    
    // Welcome Email after Registration
    public void sendWelcomeEmail(String toEmail, String username) {
        String subject = "Welcome to " + gymName + "!";
        String content = String.format(
            "Dear %s,\n\n" +
            "Welcome to %s! We're excited to have you as a member.\n\n" +
            "Your account has been successfully created. You can now:\n" +
            "1. Log in to your account\n" +
            "2. View your membership details\n" +
            "3. Schedule training sessions\n" +
            "4. Make payments online\n\n" +
            "If you have any questions, please don't hesitate to contact us.\n\n" +
            "Best regards,\n" +
            "%s Team",
            username, gymName, gymName
        );
        
        sendSimpleEmail(toEmail, subject, content);
    }
    
    // Payment Success Email with Receipt
    public void sendPaymentSuccessEmail(String toEmail, String memberName, Double amount, 
                                        String paymentDate, String paymentMethod, String transactionId) {
        String subject = "Payment Receipt - " + gymName;
        String content = String.format(
            "Dear %s,\n\n" +
            "Thank you for your payment to %s.\n\n" +
            "Payment Details:\n" +
            "----------------\n" +
            "Amount: $%.2f\n" +
            "Date: %s\n" +
            "Payment Method: %s\n" +
            "Transaction ID: %s\n" +
            "Status: Completed\n\n" +
            "This payment has been successfully processed and your membership remains active.\n\n" +
            "You can view your payment history by logging into your account.\n\n" +
            "Best regards,\n" +
            "%s Team",
            memberName, gymName, amount, paymentDate, paymentMethod, transactionId, gymName
        );
        
        sendSimpleEmail(toEmail, subject, content);
    }
    
    // Membership Expiry Reminder (7 days before expiry)
    public void sendMembershipExpiryReminder(String toEmail, String memberName, String expiryDate) {
        String subject = "Membership Expiry Reminder - " + gymName;
        String content = String.format(
            "Dear %s,\n\n" +
            "This is a friendly reminder that your membership at %s will expire in 7 days.\n\n" +
            "Expiry Date: %s\n\n" +
            "To continue enjoying our facilities and services without interruption, " +
            "please renew your membership before the expiry date.\n\n" +
            "You can renew your membership:\n" +
            "1. Online through your account\n" +
            "2. At the front desk\n" +
            "3. By calling our customer service\n\n" +
            "If you have already renewed, please ignore this email.\n\n" +
            "Best regards,\n" +
            "%s Team",
            memberName, gymName, expiryDate, gymName
        );
        
        sendSimpleEmail(toEmail, subject, content);
    }
    
    // Password Reset Email with OTP
    public void sendPasswordResetEmail(String toEmail, String username, String otp) {
        String subject = "Password Reset Request - " + gymName;
        String content = String.format(
            "Dear %s,\n\n" +
            "You have requested to reset your password for your %s account.\n\n" +
            "Your One-Time Password (OTP) is: %s\n\n" +
            "This OTP is valid for 10 minutes. Please enter it on the password reset page " +
            "to set a new password.\n\n" +
            "If you did not request a password reset, please ignore this email or contact " +
            "our support team immediately.\n\n" +
            "Best regards,\n" +
            "%s Team",
            username, gymName, otp, gymName
        );
        
        sendSimpleEmail(toEmail, subject, content);
    }
    // Password Changed Confirmation Email
public void sendPasswordChangedEmail(String toEmail, String username) {

    String subject = "Password Changed Successfully - " + gymName;

    String content = String.format(
            "Dear %s,\n\n" +
            "Your password has been changed successfully.\n\n" +
            "If you did not perform this action, please contact our support team immediately.\n\n" +
            "Best Regards,\n" +
            "%s Team",
            username,
            gymName
    );

    sendSimpleEmail(toEmail, subject, content);
}
    
    // Contact Us Email (Sent to admin when user submits contact form)
    public void sendContactUsEmail(String fromName, String fromEmail, String subject, String message) {
        String adminEmail = "admin@gymmanagement.com";
        String emailSubject = "Contact Us Inquiry: " + subject;
        String content = String.format(
            "You have received a new contact us inquiry:\n\n" +
            "From: %s\n" +
            "Email: %s\n" +
            "Subject: %s\n\n" +
            "Message:\n%s\n\n" +
            "Please respond to this inquiry within 24 hours.",
            fromName, fromEmail, subject, message
        );
        
        sendSimpleEmail(adminEmail, emailSubject, content);
        
        // Send acknowledgment to user
        String userAckSubject = "We've received your inquiry - " + gymName;
        String userAckContent = String.format(
            "Dear %s,\n\n" +
            "Thank you for contacting %s.\n\n" +
            "We have received your inquiry with the subject: \"%s\"\n\n" +
            "Our team will review your message and get back to you within 24 hours.\n\n" +
            "Best regards,\n" +
            "%s Team",
            fromName, gymName, subject, gymName
        );
        
        sendSimpleEmail(fromEmail, userAckSubject, userAckContent);
    }
    
    // Helper method to send simple email
   private void sendSimpleEmail(String to, String subject, String text) {

    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromEmail);
    message.setTo(to);
    message.setSubject(subject);
    message.setText(text);

    System.out.println("Sending mail to: " + to);

    mailSender.send(message);

    System.out.println("Mail Sent Successfully");
}
    
    // Helper method to send HTML email (if needed in future)
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("HTML email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send HTML email to " + to + ": " + e.getMessage());
        }
    }
    
    // Utility method to generate OTP
    public String generateOTP() {
        // Generate a 6-digit OTP
        return String.format("%06d", (int) (Math.random() * 1000000));
    }
    
    // Method to send membership expiry reminders (to be called by a scheduler)
    public void sendScheduledExpiryReminders() {
        // This method would typically query the database for members whose membership
        // expires in 7 days and send reminders
        // Implementation would require a membership expiry date field in Member entity
        System.out.println("Scheduled membership expiry reminder check executed at: " + LocalDate.now());
    }
}