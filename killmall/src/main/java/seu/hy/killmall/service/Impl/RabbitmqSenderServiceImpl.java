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
import seu.hy.killmall.service.RabbitmqSenderService;

@Service
public class RabbitmqSenderServiceImpl implements RabbitmqSenderService{
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

    /**
     * 秒杀成功后生成抢购订单--发送信息到死信队列，等待着一定超时未支付的订单
     * */
    @Override
    public void senderKillSuccessOrderExpireMsg(String orderNo) {
        try {
            if(StringUtils.hasLength(orderNo)){
                    //通过订单编号取出订单信息封装到对象中
                KillSuccessUserInfo info = itemKillSuccessMapper.selectByCode(orderNo);
                if(info!=null){
                    //将对象封装到对象中发送到死信队列上
                    //设置消息的传输格式
                    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
                    //设定交换机
                    rabbitTemplate.setExchange(env.getProperty("mq.kill.item.success.kill.dead.prod.exchange"));
                    //设置路由
                    rabbitTemplate.setRoutingKey(env.getProperty("mq.kill.item.success.kill.dead.prod.routing.key"));
                    //发送消息
                    System.out.println("发送死信消息");
                    rabbitTemplate.convertAndSend(info, new MessagePostProcessor() {
                        @Override
                        public Message postProcessMessage(Message message) throws AmqpException {
                            MessageProperties mp=message.getMessageProperties();
                            //设置消息头，保证接受者可以接受消息顺利封装到对象中
                            mp.setHeader(AbstractJavaTypeMapper.DEFAULT_CONTENT_CLASSID_FIELD_NAME,KillSuccessUserInfo.class);
                            //设置消息传输模式，持久化，保证消息传输的可靠性
                            mp.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                            //动态设置TTL(为了测试方便，暂且设置10s)
                            mp.setExpiration(env.getProperty("mq.kill.item.success.kill.expire"));
                            return message;
                        }
                    });
                }
            }
        }catch (Exception e){
            System.out.println("秒杀成功生成抢购订单-发送消息至死信队列失败"+e);
        }
    }
}
