package org.superchat.server.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.superchat.server.chat.domain.dto.ChatMsgRecallDTO;

@Getter
public class MessageRecallEvent extends ApplicationEvent {
    private final ChatMsgRecallDTO chatMsgRecallDTO;

    public MessageRecallEvent(Object source,ChatMsgRecallDTO dto) {
        super(source);
        this.chatMsgRecallDTO=dto;
    }
}
