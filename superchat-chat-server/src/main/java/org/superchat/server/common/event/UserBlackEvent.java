package org.superchat.server.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.superchat.server.user.domain.entity.User;

@Getter
public class UserBlackEvent extends ApplicationEvent {
    private  User user;

    public UserBlackEvent(Object source, User user) {
        super(source);
        this.user=user;
    }

}
