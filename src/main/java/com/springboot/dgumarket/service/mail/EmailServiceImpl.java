package com.springboot.dgumarket.service.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Created by TK YOUN (2020-11-09 오전 8:52)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */

@Component
public class EmailServiceImpl implements EmailService {

    @Autowired
    public JavaMailSender emailSender;

    @Override
    public void send(String to) throws MailException {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("donggukmk@gmail.com");
        message.setTo(to);
        message.setSubject("제목");
        message.setText("https://trello.com/b/WDriAMZ5/tk");

        emailSender.send(message);
    }
}

