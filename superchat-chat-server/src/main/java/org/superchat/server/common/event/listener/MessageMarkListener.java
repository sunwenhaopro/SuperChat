package org.superchat.server.common.event.listener;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.superchat.server.chat.dao.MessageDao;
import org.superchat.server.chat.dao.MessageMarkDao;
import org.superchat.server.chat.domain.dto.ChatMessageMarkDTO;
import org.superchat.server.chat.domain.entity.Message;
import org.superchat.server.chat.domain.enums.MessageMarkTypeEnum;
import org.superchat.server.chat.domain.enums.MessageTypeEnum;
import org.superchat.server.common.domain.enums.IdempotentEnum;
import org.superchat.server.common.event.MessageMarkEvent;
import org.superchat.server.common.service.PushService;
import org.superchat.server.user.domain.enums.ItemEnum;
import org.superchat.server.user.service.UserBackpackService;
import org.superchat.server.user.service.convert.WSConvert;

import java.util.Objects;

@Component
@AllArgsConstructor
public class MessageMarkListener {
    private final MessageMarkDao messageMarkDao;
    private final MessageDao messageDao;
    private final UserBackpackService userBackpackService;
    private final PushService pushService;

    @Async
    @TransactionalEventListener(classes = MessageMarkEvent.class,fallbackExecution = true)
    public void changeMsgType(MessageMarkEvent event)
    {
       ChatMessageMarkDTO dto=event.getChatMessageMarkDTO();
        Message msg=messageDao.getById(dto.getMsgId());
        if (!Objects.equals(msg.getType(), MessageTypeEnum.TEXT.getType())) {//普通消息才需要升级
            return;
        }
        //消息被标记次数
        Integer markCount = messageMarkDao.getMarkCount(dto.getMsgId(), dto.getMarkType());
        MessageMarkTypeEnum markTypeEnum = MessageMarkTypeEnum.of(dto.getMarkType());
        if (markCount < markTypeEnum.getRiseNum()) {
            return;
        }
        if (MessageMarkTypeEnum.LIKE.getType().equals(dto.getMarkType())) {//尝试给用户发送一张徽章
            userBackpackService.acquireItem(msg.getFromUid(), ItemEnum.LIKE_BADGE.getId(), IdempotentEnum.MSG_ID, msg.getId().toString());
        }
    }

    @Async
    @EventListener(classes = MessageMarkEvent.class)
    public void notifyAll(MessageMarkEvent event)
    {
        ChatMessageMarkDTO dto=event.getChatMessageMarkDTO();
        Integer markCount=messageMarkDao.getMarkCount(dto.getMsgId(),dto.getMarkType());
        pushService.sendPushMsg(WSConvert.toMsgMarkSend(dto,markCount));
    }



}
