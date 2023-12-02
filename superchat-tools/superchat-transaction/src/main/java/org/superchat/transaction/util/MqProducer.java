package org.superchat.transaction.util;

import lombok.AllArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.superchat.transaction.annotation.SecureInvoke;


public class MqProducer {
    @Autowired
    private  RocketMQTemplate rocketMQTemplate;

    public void sendMsg(String topic,Object body)
    {
        Message<Object> message= MessageBuilder.withPayload(body).build();
        rocketMQTemplate.send(topic,message);
    }

    @SecureInvoke
    public void sendMessage(String topic,Object body,Object key)
    {
        Message<Object> message=MessageBuilder.withPayload(body)
                .setHeader("KEY",key)
                .build();
        rocketMQTemplate.send(topic,message);
    }
}
