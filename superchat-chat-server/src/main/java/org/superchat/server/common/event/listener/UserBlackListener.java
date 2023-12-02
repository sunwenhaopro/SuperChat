package org.superchat.server.common.event.listener;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.superchat.server.common.event.UserBlackEvent;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.vo.response.ws.WSBlack;
import org.superchat.server.user.service.WebSocketService;

@Component
@AllArgsConstructor
public class UserBlackListener {
    private final WebSocketService webSocketService;


    @TransactionalEventListener(classes = UserBlackListener.class,fallbackExecution = true)
    public void broadcastMsg(UserBlackEvent userBlackEvent)
    {
        User blackUser=userBlackEvent.getUser();
        WSBlack wsBlack=new WSBlack(blackUser.getId());
        webSocketService.broadcastMsg(wsBlack);
    }
}
