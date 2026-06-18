package com.fitlife.mail.service;

public interface EmailService {

    void sendSimpleMail(String to, String subject, String content);
}