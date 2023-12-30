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
            log.info("[sendSimpleMessage] - start: subject={}, from={}, to={}", subject, from, to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom(from);
            message.setSubject(subject);
            message.setText(text);
            emailSender.send(message);
        } catch (Exception ex) {
            log.error("[sendSimpleMessage] - failed to send mail invitation: {}", ex.getMessage(), ex);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            log.info("[sendHtmlEmail] - start: subject={}, to={}", subject, to);
            MimeMessage message = emailSender.createMimeMessage();
            message.setRecipients(Message.RecipientType.TO, to);
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            emailSender.send(message);
        } catch (Exception ex) {
            log.error("[sendHtmlEmail] - failed to send mail invitation: {}", ex.getMessage(), ex);
        }
    }

}
