package org.superchat.server.common.event.listener;

import lombok.AllArgsConstructor;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.superchat.server.common.domain.enums.IdempotentEnum;
import org.superchat.server.common.event.UserRegisterEvent;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.enums.ItemEnum;
import org.superchat.server.user.service.UserBackpackService;

@Component
@AllArgsConstructor
public class UserRegisterListener  {
    private final UserBackpackService userBackpackService;
    @EventListener(UserRegisterEvent.class)
    public void SendCard(UserRegisterEvent userRegisterEvent)
    {
        User user=userRegisterEvent.getUser();
        userBackpackService.acquireItem(user.getId(), ItemEnum.MODIFY_NAME_CARD.getId(), IdempotentEnum.UID,user.getId().toString());
    }

}
