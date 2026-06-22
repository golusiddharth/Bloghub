package com.bloghub.emailverifications;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MailUtil {

    private static final String USERNAME = System.getenv("MAIL_USERNAME");
    private static final String PASSWORD = System.getenv("MAIL_PASSWORD");

    public static void sendOTP(String toEmail, String otp) {
        System.out.println("========= MAIL DEBUG START =========");
        System.out.println("HOST     = smtp-relay.brevo.com");
        System.out.println("PORT     = 587");
        System.out.println("USERNAME = " + USERNAME);
        System.out.println("PASSWORD = " + (PASSWORD == null ? "NULL" : "SET (length=" + PASSWORD.length() + ")"));
        System.out.println("TO EMAIL = " + toEmail);
        System.out.println("OTP      = " + otp);
        System.out.println("====================================");

        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp-relay.brevo.com");
            prop.put("mail.smtp.port", "587");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.starttls.required", "true");
            prop.put("mail.smtp.connectiontimeout", "15000");
            prop.put("mail.smtp.timeout", "15000");
            prop.put("mail.smtp.writetimeout", "15000");
            prop.put("mail.debug", "true");

            System.out.println("STEP 1: Properties set");

            Session session = Session.getInstance(prop, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    System.out.println("STEP AUTH: Authenticating with " + USERNAME);
                    return new PasswordAuthentication(USERNAME, PASSWORD);
                }
            });

            System.out.println("STEP 2: Session created");

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("BlogHub - OTP Verification");
            message.setText("Your OTP: " + otp + "\n\nValid for 10 minutes.\n- BlogHub Team");

            System.out.println("STEP 3: Message built — attempting Transport.send()");

            Transport.send(message);

            System.out.println("STEP 4: ========= MAIL SENT SUCCESSFULLY to " + toEmail + " =========");

        } catch (AuthenticationFailedException e) {
            System.out.println("ERROR TYPE: AuthenticationFailedException");
            System.out.println("ERROR MSG : " + e.getMessage());
        } catch (MessagingException e) {
            System.out.println("ERROR TYPE: MessagingException");
            System.out.println("ERROR MSG : " + e.getMessage());
            Throwable cause = e.getCause();
            int depth = 0;
            while (cause != null && depth < 5) {
                System.out.println("CAUSED BY  [" + depth + "]: " + cause.getClass().getName() + " - " + cause.getMessage());
                cause = cause.getCause();
                depth++;
            }
        } catch (Exception e) {
            System.out.println("ERROR TYPE: " + e.getClass().getName());
            System.out.println("ERROR MSG : " + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            System.out.println("STACKTRACE: " + sw.toString());
        }

        System.out.println("========= MAIL DEBUG END =========");
    }
}