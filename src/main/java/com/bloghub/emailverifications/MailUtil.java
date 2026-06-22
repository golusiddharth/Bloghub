package com.bloghub.emailverifications;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class MailUtil {

    private static final String USERNAME = System.getenv("MAIL_USERNAME");
    private static final String PASSWORD = System.getenv("MAIL_PASSWORD");

    // private static Session getSession() {
    //     Properties prop = new Properties();
    //     prop.put("mail.smtp.host", "smtp.gmail.com");
    //     prop.put("mail.smtp.port", "465");
    //     prop.put("mail.smtp.auth", "true");
    //     prop.put("mail.smtp.socketFactory.port", "465");
    //     prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

    //     return Session.getInstance(prop, new Authenticator() {
    //         protected PasswordAuthentication getPasswordAuthentication() {
    //             return new PasswordAuthentication(USERNAME, PASSWORD);
    //         }
    //     });
    // }
    private static Session getSession() {

    Properties prop = new Properties();

    prop.put("mail.smtp.host", "smtp.gmail.com");
    prop.put("mail.smtp.port", "587");
    prop.put("mail.smtp.auth", "true");
    prop.put("mail.smtp.starttls.enable", "true");

    prop.put("mail.smtp.connectiontimeout", "10000");
    prop.put("mail.smtp.timeout", "10000");
    prop.put("mail.smtp.writetimeout", "10000");

    return Session.getInstance(prop,
            new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            USERNAME,
                            PASSWORD
                    );
                }
            });
}

public static void sendOTP(String toEmail, String otp)
        throws MessagingException {

    System.out.println("MAIL USERNAME = " + USERNAME);
    System.out.println("PASSWORD NULL = " + (PASSWORD == null));
    System.out.println("TO EMAIL = " + toEmail);

    Message message = new MimeMessage(getSession());

    message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(toEmail));

    message.setSubject("Email Verification OTP");
    message.setText("Your OTP is: " + otp);

     try {
    Transport.send(message);
        System.out.println("MAIL SENT SUCCESSFULLY");
    } catch (Exception e) {
        System.out.println("MAIL ERROR = " + e.getMessage());
        e.printStackTrace();
        throw new MessagingException(e.getMessage());
    }

    System.out.println("MAIL SENT SUCCESSFULLY");
}
}
