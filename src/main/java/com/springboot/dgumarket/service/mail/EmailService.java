package com.springboot.dgumarket.service.mail;

import freemarker.template.TemplateException;
import org.springframework.mail.MailException;

import javax.mail.MessagingException;
import java.io.IOException;

/**
 * Created by TK YOUN (2020-11-09 오전 9:04)
 * Github : https://github.com/dgumarket/dgumarket.git
 * Description :
 */
public interface EmailService {

    void send(String to) throws MailException, MessagingException, IOException, TemplateException;

//    void send(List<MailMessage> messages) throws MailException;
}
