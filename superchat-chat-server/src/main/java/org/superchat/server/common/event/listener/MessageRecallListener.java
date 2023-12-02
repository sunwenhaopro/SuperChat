package org.superchat.server.common.event.listener;


import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.superchat.server.chat.domain.dto.ChatMsgRecallDTO;
import org.superchat.server.common.cache.MsgCache;
import org.superchat.server.common.event.MessageRecallEvent;
import org.superchat.server.common.service.PushService;
import org.superchat.server.user.service.convert.WSConvert;

@Component
@AllArgsConstructor
public class MessageRecallListener {
    private final PushService pushService;
    private final MsgCache msgCache;

    @Async
    @TransactionalEventListener(classes = MessageRecallEvent.class,fallbackExecution = true)
    public void evictMsg(MessageRecallEvent messageRecallEvent)
    {
        ChatMsgRecallDTO chatMsgRecallDTO=messageRecallEvent.getChatMsgRecallDTO();
        msgCache.evictMsg(chatMsgRecallDTO.getMsgId());
    }

    @Async
    @TransactionalEventListener(classes = MessageRecallEvent.class,fallbackExecution = true)
    public void sendToAll(MessageRecallEvent event)
    {
        pushService.sendPushMsg(WSConvert.toMsgRecall(event.getChatMsgRecallDTO()));
    }
}

