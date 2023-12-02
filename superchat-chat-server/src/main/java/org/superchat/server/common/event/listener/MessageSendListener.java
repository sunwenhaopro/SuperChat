package org.superchat.server.common.event.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.superchat.server.common.constant.MQConstant;
import org.superchat.server.common.domain.dto.MsgSendMessageDTO;
import org.superchat.server.common.event.MessageSendEvent;
import org.superchat.transaction.util.MqProducer;

@AllArgsConstructor
@Component
@Slf4j
public class MessageSendListener {
    private final MqProducer mqProducer;
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT,classes = MessageSendEvent.class,fallbackExecution = true)
    public void messageRoute(MessageSendEvent event)
    {
        Long msgId= event.getMsgId();
        mqProducer.sendMessage(MQConstant.SEND_MSG_TOPIC,new MsgSendMessageDTO(msgId),msgId);
    }
}
