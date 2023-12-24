package com.mainthreadlab.weinv.service;

public interface EmailService {

    void sendHtmlEmail(String to, String subject, String htmlContent);

    void sendSimpleMessage(String from, String[] to, String subject, String text);
}
