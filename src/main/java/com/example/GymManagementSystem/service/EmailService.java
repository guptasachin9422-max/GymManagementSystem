package com.example.GymManagementSystem.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // ==============================
    // RESEND CONFIGURATION
    // ==============================

    @Value("${resend.api.key:}")
    private String resendApiKey;

    @Value("${app.email.from:onboarding@resend.dev}")
    private String fromEmail;

    @Value("${app.email.admin:onboarding@resend.dev}")
    private String adminEmail;

    @Value("${app.gym.name:FitLife Gym}")
    private String gymName;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();


    // ==============================
    // WELCOME EMAIL
    // ==============================

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
                username,
                gymName,
                gymName
        );

        sendSimpleEmail(toEmail, subject, content, null);
    }


    // ==============================
    // PAYMENT SUCCESS EMAIL
    // ==============================

    public void sendPaymentSuccessEmail(
            String toEmail,
            String memberName,
            Double amount,
            String paymentDate,
            String paymentMethod,
            String transactionId) {

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
                memberName,
                gymName,
                amount,
                paymentDate,
                paymentMethod,
                transactionId,
                gymName
        );

        sendSimpleEmail(toEmail, subject, content, null);
    }


    // ==============================
    // MEMBERSHIP EXPIRY REMINDER
    // ==============================

    public void sendMembershipExpiryReminder(
            String toEmail,
            String memberName,
            String expiryDate) {

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
                memberName,
                gymName,
                expiryDate,
                gymName
        );

        sendSimpleEmail(toEmail, subject, content, null);
    }


    // ==============================
    // PASSWORD RESET EMAIL
    // ==============================

    public void sendPasswordResetEmail(
            String toEmail,
            String username,
            String otp) {

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
                username,
                gymName,
                otp,
                gymName
        );

        sendSimpleEmail(toEmail, subject, content, null);
    }


    // ==============================
    // PASSWORD CHANGED EMAIL
    // ==============================

    public void sendPasswordChangedEmail(
            String toEmail,
            String username) {

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

        sendSimpleEmail(toEmail, subject, content, null);
    }


    // ==============================
    // CONTACT US EMAIL
    // ==============================

    public void sendContactUsEmail(
            String fromName,
            String fromEmail,
            String subject,
            String message) {

        String emailSubject = "Contact Us Inquiry: " + subject;

        String content = String.format(
                "You have received a new contact us inquiry:\n\n" +
                "From: %s\n" +
                "Email: %s\n" +
                "Subject: %s\n\n" +
                "Message:\n%s\n\n" +
                "Please respond to this inquiry within 24 hours.",
                fromName,
                fromEmail,
                subject,
                message
        );

        // Send inquiry to admin
        sendSimpleEmail(
                adminEmail,
                emailSubject,
                content,
                fromEmail
        );

        // Send acknowledgement to user
        String userAckSubject =
                "We've received your inquiry - " + gymName;

        String userAckContent = String.format(
                "Dear %s,\n\n" +
                "Thank you for contacting %s.\n\n" +
                "We have received your inquiry with the subject: \"%s\"\n\n" +
                "Our team will review your message and get back to you within 24 hours.\n\n" +
                "Best regards,\n" +
                "%s Team",
                fromName,
                gymName,
                subject,
                gymName
        );

        sendSimpleEmail(
                fromEmail,
                userAckSubject,
                userAckContent,
                null
        );
    }


    // ==============================
    // RESEND API EMAIL METHOD
    // ==============================

    private void sendSimpleEmail(
            String to,
            String subject,
            String text,
            String replyTo) {

        if (resendApiKey == null || resendApiKey.isBlank()) {

            System.err.println(
                    "RESEND_API_KEY is not configured. Email not sent."
            );

            return;
        }

        try {

            Map<String, Object> emailData = new HashMap<>();

            emailData.put("from", fromEmail);
            emailData.put("to", List.of(to));
            emailData.put("subject", subject);
            emailData.put("text", text);

            if (replyTo != null && !replyTo.isBlank()) {
                emailData.put("reply_to", List.of(replyTo));
            }

            String jsonBody =
                    objectMapper.writeValueAsString(emailData);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(
                                    "https://api.resend.com/emails"
                            ))
                            .header(
                                    "Authorization",
                                    "Bearer " + resendApiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(jsonBody)
                            )
                            .build();

            System.out.println(
                    "Sending email to: " + to
            );

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() >= 200
                    && response.statusCode() < 300) {

                System.out.println(
                        "Email sent successfully to: " + to
                );

            } else {

                System.err.println(
                        "Failed to send email. Status: "
                                + response.statusCode()
                );

                System.err.println(
                        "Resend response: "
                                + response.body()
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error sending email to "
                            + to
                            + ": "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }


    // ==============================
    // HTML EMAIL METHOD
    // ==============================

    private void sendHtmlEmail(
            String to,
            String subject,
            String htmlContent) {

        if (resendApiKey == null || resendApiKey.isBlank()) {

            System.err.println(
                    "RESEND_API_KEY is not configured. HTML email not sent."
            );

            return;
        }

        try {

            Map<String, Object> emailData = new HashMap<>();

            emailData.put("from", fromEmail);
            emailData.put("to", List.of(to));
            emailData.put("subject", subject);
            emailData.put("html", htmlContent);

            String jsonBody =
                    objectMapper.writeValueAsString(emailData);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(
                                    "https://api.resend.com/emails"
                            ))
                            .header(
                                    "Authorization",
                                    "Bearer " + resendApiKey
                            )
                            .header(
                                    "Content-Type",
                                    "application/json"
                            )
                            .POST(
                                    HttpRequest.BodyPublishers
                                            .ofString(jsonBody)
                            )
                            .build();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString()
                    );

            if (response.statusCode() >= 200
                    && response.statusCode() < 300) {

                System.out.println(
                        "HTML email sent successfully to: "
                                + to
                );

            } else {

                System.err.println(
                        "Failed to send HTML email. Status: "
                                + response.statusCode()
                );

                System.err.println(
                        "Resend response: "
                                + response.body()
                );
            }

        } catch (Exception e) {

            System.err.println(
                    "Error sending HTML email to "
                            + to
                            + ": "
                            + e.getMessage()
            );

            e.printStackTrace();
        }
    }


    // ==============================
    // GENERATE OTP
    // ==============================

    public String generateOTP() {

        // Generate 6-digit OTP
        return String.format(
                "%06d",
                (int) (Math.random() * 1000000)
        );
    }


    // ==============================
    // SCHEDULED EXPIRY REMINDERS
    // ==============================

    public void sendScheduledExpiryReminders() {

        System.out.println(
                "Scheduled membership expiry reminder check executed at: "
                        + LocalDate.now()
        );
    }
}