package seu.hy.killmall.service.Impl;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.AbstractJavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import seu.hy.killmall.mapper.ItemKillSuccessMapper;
import seu.hy.killmall.pojo.KillSuccessUserInfo;
import seu.hy.killmall.service.EmailSenderService;

@Service
public class EmailSenderServiceImpl implements EmailSenderService{
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    @Autowired
    private ItemKillSuccessMapper itemKillSuccessMapper;
    @Override
    public void senderKillSuccessEmail(String orderNo) {
        System.out.println("秒杀成功异步发送通知消息--准备发送消息"+orderNo);
        try {
            if(StringUtils.hasLength(orderNo)){
                KillSuccessUserInfo info= itemKillSuccessMapper.selectByCode(orderNo);
                System.out.println(info);
                //rabbitmq发送消息逻辑
                rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                rabbitTemplate.setExchange(env.getProperty("mq.kill.item.success.email.exchange"));
                rabbitTemplate.setRoutingKey(env.getProperty("mq.kill.item.success.email.routing.key"));
                //将info充当消息发送到队列中
                rabbitTemplate.convertAndSend(info, new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        MessageProperties messageProperties =message.getMessageProperties();
                        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        messageProperties.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);
                        return message;
                    }
                });
            }
        }catch (Exception e){
            System.out.println("秒杀异常"+e);
        }
    }
}
