package com.bloghub.emailverifications;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class MailUtil {

    private static final String USERNAME = System.getenv("MAIL_USERNAME");
    private static final String PASSWORD = System.getenv("MAIL_PASSWORD");

    private static Session getSession() {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.starttls.required", "true");
        prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        return Session.getInstance(prop, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    public static void sendOTP(String toEmail, String otp) {
        System.out.println("MAIL USERNAME = " + USERNAME);
        System.out.println("PASSWORD NULL = " + (PASSWORD == null));
        System.out.println("TO EMAIL = " + toEmail);

        try {
            Message message = new MimeMessage(getSession());
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(toEmail)
            );
            message.setSubject("BlogHub - Email Verification OTP");

            // HTML email
            String htmlContent =
                "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:32px;background:#f9f9f9;border-radius:12px;'>" +
                "<h2 style='color:#6d28d9;margin-bottom:8px;'>BlogHub</h2>" +
                "<p style='color:#333;font-size:15px;'>Your OTP for email verification:</p>" +
                "<div style='font-size:36px;font-weight:bold;letter-spacing:8px;color:#6d28d9;background:#ede9fe;padding:16px 24px;border-radius:8px;display:inline-block;margin:16px 0;'>" + otp + "</div>" +
                "<p style='color:#666;font-size:13px;'>This OTP is valid for <b>10 minutes</b>. Do not share it with anyone.</p>" +
                "<p style='color:#999;font-size:12px;margin-top:24px;'>If you did not request this, please ignore this email.</p>" +
                "</div>";

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");

            Multipart multipart = new MimeMultipart("alternative");
            multipart.addBodyPart(htmlPart);
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("OTP MAIL SENT SUCCESSFULLY to " + toEmail);

        } catch (Exception e) {
            System.out.println("MAIL SEND ERROR:");
            e.printStackTrace();
        }
    }
}