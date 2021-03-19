package com.springboot.dgumarket.service.mail;

import org.springframework.mail.MailException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;

import java.util.List;

/**
 * Created by TK YOUN (2020-11-09 오전 9:04)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface EmailService {

    void send(String to) throws MailException;

//    void send(List<MailMessage> messages) throws MailException;
}
