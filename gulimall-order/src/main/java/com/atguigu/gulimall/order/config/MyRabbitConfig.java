package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.PostConstruct;

@Configuration
public class MyRabbitConfig {


    @Autowired
    RabbitTemplate rabbitTemplate;

    //使用JSON序列化机制，进行消息转换
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }

    //定制RabbitTemplate
    @PostConstruct//MyRabbitConfig创建完成后执行这个方法
    public void initRabbitTemplate(){


        //设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /*只要消息抵达Broker ack就是true
            * correlationData  当前消息的唯一关联数据（这个是消息的唯一id）
            * ack  消息是否成功收到
            * cause  失败的原因
            * */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
//                System.out.println("confirm..." + correlationData + ack + cause);

            }
        });




        //设置消息抵达队列的确认回调
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /*只要消息没有投递给指定的队列，就触发这个失败回调
             * message  投递失败的消息详细信息
             * replyCode   回复的状态码
             * replyText   回复的文本内容
             * exchange    当时这个消息发给哪个交换机
             * routingKey  当时这个消息用的路由键
             * */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
            }
        });



    }
}
