package seu.hy.killmall.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import seu.hy.killmall.pojo.KillSuccessUserInfo;

@Service
public class EmailReciveService {

    @RabbitListener(queues = {"test.kill.item.success.email.queue"},containerFactory = "singleListenerContainer")
    public void consumerEmailMsg(KillSuccessUserInfo info) {
        try {
            System.out.println("接受消息为："+info);
        }catch (Exception e){
            System.out.println("接受消息出现异常"+e);
        }
    }

}
