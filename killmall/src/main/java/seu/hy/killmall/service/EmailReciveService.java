package seu.hy.killmall.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Service;
import seu.hy.killmall.dto.MailDto;
import seu.hy.killmall.pojo.KillSuccessUserInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

@Service
public class EmailReciveService {

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
    private MailService mailService;

    @RabbitListener(queues = {"test.kill.item.success.email.queue"},containerFactory = "singleListenerContainer")
    public void consumerEmailMsg(KillSuccessUserInfo info) {
        try {
            MailDto mailDto=new MailDto();
            mailDto.setTos(new String[]{info.getEmail()});
            final String content=String.format(pro.getProperty("mail.kill.item.success.content"),info.getItemName(),info.getCode());
            mailDto.setSubject(pro.getProperty("mail.kill.item.success.subject"));
            mailDto.setContent(content);
            //调用发送邮件服务
            mailService.sendHtmlMail(mailDto);
            System.out.println("接受消息为："+info);
        }catch (Exception e){
            System.out.println("接受消息出现异常"+e);
        }
    }

}
