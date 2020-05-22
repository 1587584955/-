package seu.hy.killmall.config;

import com.google.common.collect.Maps;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Map;

//通用化的rabbitmq配置
@Configuration
public class RabbitmqConfig {

    @Autowired
    private Environment env;

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private SimpleRabbitListenerContainerFactoryConfigurer factoryConfigurer;

    /**
     * 单一消费者
     * @return
     */
    @Bean(name = "singleListenerContainer")
    public SimpleRabbitListenerContainerFactory listenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setPrefetchCount(1);
        factory.setTxSize(1);
        return factory;
    }


    /**
     * 多个消费者
     * @return
     */
    @Bean(name = "multiListenerContainer")
    public SimpleRabbitListenerContainerFactory multiListenerContainer(){
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factoryConfigurer.configure(factory,connectionFactory);
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //确认消费模式-NONE
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        factory.setConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.simple.concurrency",int.class));
        factory.setMaxConcurrentConsumers(env.getProperty("spring.rabbitmq.listener.simple.max-concurrency",int.class));
        factory.setPrefetchCount(env.getProperty("spring.rabbitmq.listener.simple.prefetch",int.class));
        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(){
        connectionFactory.setPublisherConfirms(true);
        connectionFactory.setPublisherReturns(true);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("消息发送成功:correlationData({"+correlationData+"}),ack({"+ack+"}),cause({"+cause+"})");
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                System.out.println("消息丢失:exchange({"+exchange+"}),route({"+routingKey+"}),replyCode({"+replyCode+"}),replyText({"+replyText+"}),message:{"+message+"}");
            }
        });
        return rabbitTemplate;
    }
    //构建异步发送邮箱通知的消息模型
    @Bean
    public Queue successEmailQueue(){
        return new Queue(env.getProperty("mq.kill.item.success.email.queue"),true);
    }

    @Bean
    public TopicExchange successEmailExchange(){
        return new TopicExchange(env.getProperty("mq.kill.item.success.email.exchange"),true,false);
    }

    @Bean
    public Binding successEmailBinding(){
        return BindingBuilder.bind(successEmailQueue()).to(successEmailExchange()).with(env.getProperty("mq.kill.item.success.email.routing.key"));
    }


    //构建秒杀成功后-订单超时未支付的死信队列消息模型
    /**
     * 死信队列由死信交换机、死信路由和TTL-存活时间组成（属性）
     * 将死信交换机绑定到基本交换机和基本路由上
     * 再将真正的消息队列绑定到死信交换机上
     * 首先将消息通过基本路发送到基本交换机上，在存到死信队列上，等到存活时间超过阈值
     * 就将将死信队列上的消息通过死信路由死信交换机发送到真正队列上
     * 消费者监听真正队列，获取超过ttl的消息
     * */
    //构建死信队列模型
    @Bean
    public Queue successKillDeadQueue(){
        Map<String,Object> argsMap= Maps.newHashMap();
        //死信队列绑定死信交换机
//      argsMap.put("x-dead-letter-exchange",env.getProperty("mq.kill.item.success.kill.dead.exchange"));
        argsMap.put("x-dead-letter-exchange",env.getProperty("mq.kill.item.success.kill.dead.exchange"));
        argsMap.put("x-dead-letter-routing-key",env.getProperty("mq.kill.item.success.kill.dead.routing.key"));
        //绑定死信路由
        //argsMap.put("x-dead-letter-routing-key",env.getProperty("mq.kill.item.success.kill.dead.routing.key"));
        //绑定TTL-存活参数,在这里在暂时不设置，一会动态设置
        //argsMap.put("x-message-ttl",10000);
        return new Queue(env.getProperty("mq.kill.item.success.kill.dead.queue"),true,false,false,argsMap);
    }

    //创建基本交换机
    @Bean
    public  TopicExchange successKillDeadProdExchange(){
        return new TopicExchange(env.getProperty("mq.kill.item.success.kill.dead.prod.exchange"),true,false);
    }

    //创建基本交换机+基本路由->死信队列的绑定
    @Bean
    public Binding suceessKillDeadProdBinding(){
        return BindingBuilder.bind(successKillDeadQueue()).to(successKillDeadProdExchange()).with(env.getProperty("mq.kill.item.success.kill.dead.prod.routing.key"));
    }

    //创建真实队列
    @Bean
    public Queue successKillRealQueue(){
        return  new Queue(env.getProperty("mq.kill.item.success.kill.dead.real.queue"),true);
    }

    //构建死信交换机模型
    @Bean
    public  TopicExchange successKillDeadExchange(){
        return new TopicExchange(env.getProperty("mq.kill.item.success.kill.dead.exchange"),true,false);
    }

    //死信交换机+死信路由->真正队列 的绑定
    @Bean
    public Binding successKillDeadBinding(){
        return BindingBuilder.bind(successKillRealQueue()).to(successKillDeadExchange()).with(env.getProperty("mq.kill.item.success.kill.dead.routing.key"));
    }







}

