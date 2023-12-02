package org.superchat.server.common.consumer;

import lombok.AllArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.tomcat.websocket.server.WsServerContainer;
import org.springframework.stereotype.Component;
import org.superchat.server.chat.domain.vo.response.ChatMessageResp;
import org.superchat.server.common.constant.MQConstant;
import org.superchat.server.common.domain.dto.PushMessageDTO;
import org.superchat.server.user.domain.enums.WSBaseResp;
import org.superchat.server.user.domain.enums.WSPushTypeEnum;
import org.superchat.server.user.service.WebSocketService;

@AllArgsConstructor
@Component
@RocketMQMessageListener(topic = MQConstant.PUSH_TOPIC,consumerGroup = MQConstant.PUSH_GROUP)
public class PushConsumer implements RocketMQListener<PushMessageDTO> {

    private final WebSocketService webSocketService;
    @Override
    public void onMessage(PushMessageDTO message) {
        WSPushTypeEnum wsPushTypeEnum=WSPushTypeEnum.of(message.getPushType());
        switch (wsPushTypeEnum)
        {
            case USER:
                message.getUidList().forEach(uid->{
                    webSocketService.sendToUser(uid,message.getWsBaseMsg());
                });
                break;
            case ALL:
                webSocketService.sendToAllOnline(message.getWsBaseMsg(),null);
        }
    }
}
