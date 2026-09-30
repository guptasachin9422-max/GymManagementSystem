package com.example.GymManagementSystem.service;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.admin}")
    private String adminEmail;

    @Value("${app.gym.name:FitLife Gym}")
    private String gymName;

    // Welcome email
    public void sendWelcomeEmail(String toEmail, String username) {

        String subject = "Welcome to " + gymName + "!";

        String content =
                "Dear " + username + ",\n\n" +
                "Welcome to " + gymName + "!\n\n" +
                "Your account has been successfully created.\n\n" +
                "You can now:\n" +
                "1. Log in to your account\n" +
                "2. View your membership details\n" +
                "3. Schedule training sessions\n" +
                "4. Make payments online\n\n" +
                "Thank you for joining us.\n\n" +
                "Best regards,\n" +
                gymName + " Team";

        sendEmail(toEmail, subject, content, null);
    }

    // Payment success email
    public void sendPaymentSuccessEmail(
            String toEmail,
            String memberName,
            Double amount,
            String paymentDate,
            String paymentMethod,
            String transactionId) {

        String subject = "Payment Receipt - " + gymName;

        String content =
                "Dear " + memberName + ",\n\n" +
                "Thank you for your payment to " + gymName + ".\n\n" +
                "Payment Details:\n" +
                "----------------------\n" +
                "Amount: ₹" + amount + "\n" +
                "Date: " + paymentDate + "\n" +
                "Payment Method: " + paymentMethod + "\n" +
                "Transaction ID: " + transactionId + "\n" +
                "Status: Completed\n\n" +
                "Your payment has been successfully processed.\n\n" +
                "Best regards,\n" +
                gymName + " Team";

        sendEmail(toEmail, subject, content, null);
    }

    // Membership expiry reminder
    public void sendMembershipExpiryReminder(
            String toEmail,
            String memberName,
            String expiryDate) {

        String subject = "Membership Expiry Reminder - " + gymName;

        String content =
                "Dear " + memberName + ",\n\n" +
                "This is a reminder that your membership at " +
                gymName + " will expire soon.\n\n" +
                "Expiry Date: " + expiryDate + "\n\n" +
                "Please renew your membership to continue using our services.\n\n" +
                "Best regards,\n" +
                gymName + " Team";

        sendEmail(toEmail, subject, content, null);
    }

    // Password reset email
    public void sendPasswordResetEmail(
            String toEmail,
            String username,
            String otp) {

        String subject = "Password Reset OTP - " + gymName;

        String content =
                "Dear " + username + ",\n\n" +
                "Your password reset OTP is:\n\n" +
                otp + "\n\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "Best regards,\n" +
                gymName + " Team";

        sendEmail(toEmail, subject, content, null);
    }

    // Password changed email
    public void sendPasswordChangedEmail(
            String toEmail,
            String username) {

        String subject = "Password Changed Successfully - " + gymName;

        String content =
                "Dear " + username + ",\n\n" +
                "Your password has been changed successfully.\n\n" +
                "If you did not perform this action, please contact our support team.\n\n" +
                "Best regards,\n" +
                gymName + " Team";

        sendEmail(toEmail, subject, content, null);
    }

    // Contact us email
    public void sendContactUsEmail(
            String fromName,
            String fromEmailAddress,
            String subject,
            String message) {

        String adminSubject = "Contact Us Inquiry: " + subject;

        String adminContent =
                "You have received a new contact us inquiry.\n\n" +
                "Name: " + fromName + "\n" +
                "Email: " + fromEmailAddress + "\n" +
                "Subject: " + subject + "\n\n" +
                "Message:\n" +
                message;

        sendEmail(adminEmail, adminSubject, adminContent, fromEmailAddress);

        String userSubject = "We've received your inquiry - " + gymName;

        String userContent =
                "Dear " + fromName + ",\n\n" +
                "Thank you for contacting " + gymName + ".\n\n" +
                "We have received your inquiry and will get back to you soon.\n\n" +
                "Best regards,\n" +
                gymName + " Team";

        sendEmail(fromEmailAddress, userSubject, userContent, null);
    }

    // Main SMTP sending method
    private void sendEmail(
            String to,
            String subject,
            String text,
            String replyTo) {

        try {

            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            if (replyTo != null && !replyTo.isBlank()) {
                message.setReplyTo(replyTo);
            }

            System.out.println("Sending email to: " + to);

            mailSender.send(message);

            System.out.println("Email sent successfully to: " + to);

        } catch (Exception e) {

            System.err.println(
                    "Failed to send email to " + to +
                    ": " + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    public String generateOTP() {
        return String.format(
                "%06d",
                (int) (Math.random() * 1000000)
        );
    }

    public void sendScheduledExpiryReminders() {

        System.out.println(
                "Scheduled membership expiry reminder check executed at: "
                        + LocalDate.now()
        );
    }
}
