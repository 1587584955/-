package seu.hy.killmall.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import seu.hy.killmall.dto.MailDto;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;
@Service
@EnableAsync
public class MailService {
    private  static Properties pro;
    static {
        String filename="application.properties";
        pro= new Properties();
        try {
            pro.load(new InputStreamReader(MailService.class.getClassLoader().getResourceAsStream(filename),"utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    private JavaMailSender mailSender;

    /**
     * 发送简单文本邮件
     * */
    @Async
    public void sendSimpleMail(final MailDto mailDto){
        try{
            SimpleMailMessage message =new SimpleMailMessage();
            message.setFrom(pro.getProperty("mail.send.from"));
            message.setSubject(mailDto.getSubject());
            message.setTo(mailDto.getTos());
            message.setText(mailDto.getContent());
            mailSender.send(message);
            System.out.println("发送简单文本文件成功");
        }catch (Exception e){
            System.out.println("发送简单文本邮件失败"+e);
        }
    }

    /**
     * 发送带html格式的邮件
     * */

    @Async
    public void sendHtmlMail(MailDto mailDto){

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper=new MimeMessageHelper(message,true,"utf-8");
            mimeMessageHelper.setFrom(pro.getProperty("mail.send.from"));
            mimeMessageHelper.setTo(mailDto.getTos());
            mimeMessageHelper.setText(mailDto.getContent(),true);
            mimeMessageHelper.setSubject(mailDto.getSubject());
            mailSender.send(message);
            System.out.println("邮件发送成功");
        } catch (MessagingException e) {
            System.out.println("邮件发送失败"+e);
        }


    }

}





















