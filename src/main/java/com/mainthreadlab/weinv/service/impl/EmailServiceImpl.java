package com.mainthreadlab.weinv.service.impl;

import com.mainthreadlab.weinv.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;

@Slf4j
@Async
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;


    /**
     * it's possible to send mail multiple users at once
     */
    @Override
    public void sendSimpleMessage(String from, String[] to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(from);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send mail invitation: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            emailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send mail invitation: {}", ex.getMessage(), ex);
        }
    }

}
