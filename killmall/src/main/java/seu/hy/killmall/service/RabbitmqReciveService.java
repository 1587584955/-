package seu.hy.killmall.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailSender;
import org.springframework.stereotype.Service;
import seu.hy.killmall.dto.MailDto;
import seu.hy.killmall.mapper.ItemKillSuccessMapper;
import seu.hy.killmall.pojo.KillSuccessUserInfo;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

@Service
public class RabbitmqReciveService {

    private  static Properties pro;
    @Autowired
    private Environment env;
    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;
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

    /**
     * 秒杀成功后超时未支付的监听者
     * 监听真正的队列
     * */
    @RabbitListener(queues = {"${mq.kill.item.success.kill.dead.real.queue}"},containerFactory = "singleListenerContainer")
    public void consumerExprireOrder(KillSuccessUserInfo info){
        try {
            //将超时的那些订单的状态全部改成-1-失效
            if(info!=null){
               //通过主键订单号获取该笔订单的信息
                KillSuccessUserInfo entity = itemKillSuccessMapper.selectByCode(info.getCode());
                //判断条件
                if(entity!=null&&entity.getStatus().intValue()==0){
                    //修改订单号的状态信息
                    int ref = itemKillSuccessMapper.expireOrder(info.getCode());
                    if(ref>0){
                        System.out.println("修改成功");
                    }else{
                        System.out.println("修改失败");
                    }

                }
            }

        }catch (Exception e){
            System.out.println("接受死信消息队列失败"+e);
        }
    }

}
