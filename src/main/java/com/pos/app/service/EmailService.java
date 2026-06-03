package com.pos.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendTemporaryPassword(
            String email,
            String password
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("POS Password Reset");
        message.setText(
                "Your temporary password is: " + password
        );

        try {
            mailSender.send(message);
        } catch (org.springframework.mail.MailException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send email. Please check your SMTP configuration.");
        }
    }
}
