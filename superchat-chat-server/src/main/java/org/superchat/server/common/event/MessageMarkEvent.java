package org.superchat.server.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.superchat.server.chat.domain.dto.ChatMessageMarkDTO;

@Getter
public class MessageMarkEvent extends ApplicationEvent {
    private final ChatMessageMarkDTO chatMessageMarkDTO;

    public MessageMarkEvent(Object source,ChatMessageMarkDTO chatMessageMarkDTO) {
        super(source);
        this.chatMessageMarkDTO=chatMessageMarkDTO;
    }
}
