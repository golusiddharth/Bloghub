package com.bloghub.emailverifications;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class MailUtil {

    private static final String USERNAME = System.getenv("MAIL_USERNAME");
    private static final String PASSWORD = System.getenv("MAIL_PASSWORD");

    public static void sendOTP(String toEmail, String otp) {
        System.out.println("=== MAIL DEBUG START ===");
        System.out.println("MAIL USERNAME = " + USERNAME);
        System.out.println("PASSWORD NULL = " + (PASSWORD == null));
        System.out.println("TO EMAIL = " + toEmail);

        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "587");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.starttls.required", "true");
            prop.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            prop.put("mail.smtp.user", USERNAME);

            Session session = Session.getInstance(prop, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("BlogHub - Your OTP Verification Code");
            message.setText("Your BlogHub OTP is: " + otp + "\n\nValid for 10 minutes.\n\n- BlogHub Team");

            Transport transport = session.getTransport("smtp");
            transport.connect("smtp.gmail.com", 587, USERNAME, PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            System.out.println("=== OTP MAIL SENT SUCCESSFULLY to " + toEmail + " ===");

        } catch (Exception e) {
            System.out.println("=== MAIL SEND FAILED ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}