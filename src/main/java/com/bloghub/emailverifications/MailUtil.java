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
        prop.put("mail.smtp.port", "465");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.socketFactory.port", "465");
        prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        return Session.getInstance(prop, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }

    public static void sendOTP(String toEmail, String otp) throws MessagingException {
        Message message = new MimeMessage(getSession());
        message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(toEmail)
        );
        message.setSubject("Email Verification OTP");
        message.setText("Your OTP is: " + otp + "\nValid for 5 minutes.");
        Transport.send(message);
    }
}
