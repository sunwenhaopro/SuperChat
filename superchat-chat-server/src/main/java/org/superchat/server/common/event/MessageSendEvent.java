package org.superchat.server.common.event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
public class MessageSendEvent extends ApplicationEvent {

    private final Long msgId;

    public MessageSendEvent(Object source,Long msgId) {
        super(source);
        this.msgId=msgId;
    }
}
