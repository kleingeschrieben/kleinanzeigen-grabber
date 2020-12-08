package de.mpru.itemgrabber;

import org.springframework.context.annotation.Bean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

public class MailHandler {

    private String mailHost;
    private int mailPort;
    private String mailUser;
    private String mailPass;
    private String mailSmtpAuth;
    private String mailStarttls;
    private String mailReceiver;

    private JavaMailSender ms;

    public MailHandler(String mailHost, int mailPort, String mailUser, String mailPass, String mailSmtpAuth, String mailStarttls, String mailReceiver) {
        this.mailHost = mailHost;
        this.mailPort = mailPort;
        this.mailUser = mailUser;
        this.mailPass = mailPass;
        this.mailSmtpAuth = mailSmtpAuth;
        this.mailStarttls = mailStarttls;
        this.mailReceiver = mailReceiver;
        ms = getJavaMailSender();
    }

    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailHost);
        mailSender.setPort(mailPort);

        mailSender.setUsername(mailUser);
        mailSender.setPassword(mailPass);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", mailSmtpAuth);
        props.put("mail.smtp.starttls.enable", mailStarttls);
        return mailSender;
    }

    public void sendSimpleMessage(String to, String subject, String text, String from) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        ms.send(message);
    }

    public String getMailReceiver() {
        return mailReceiver;
    }

    public String getMailUser() {
        return mailUser;
    }
}
