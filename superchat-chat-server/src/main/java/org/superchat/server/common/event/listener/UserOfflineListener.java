package org.superchat.server.common.event.listener;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.superchat.server.common.event.UserOfflineEvent;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.enums.ChatActiveStatusEnum;
import org.superchat.server.user.service.ChatService;
import org.superchat.server.user.service.WebSocketService;
import org.superchat.server.common.cache.UserCache;
import org.superchat.server.user.service.convert.WSConvert;

@Component
@AllArgsConstructor
public class UserOfflineListener {
    private final UserDao userDao;
    private final UserCache userCache;
    private final WebSocketService webSocketService;
    private final ChatService chatService;

    @Async
    @EventListener(classes = UserOfflineListener.class)
    public void saveDB(UserOfflineEvent event) {
        User user = event.getUser();
        user.setActiveStatus(ChatActiveStatusEnum.OFFLINE.getStatus());
        userDao.updateById(user);
    }

    @Async
    @EventListener(classes = UserOfflineListener.class)
    public void noticeOtherUser(UserOfflineEvent userOfflineEvent) {
        User user = userOfflineEvent.getUser();
        userCache.offline(user.getId(), user.getLastOptTime());
        Long onlineNum=chatService.getMemberStatistic().getOnlineNum();
        //不通知本人
        webSocketService.sendToAllOnline(WSConvert.toOfflineNotifyResp(user,onlineNum),user.getId());
    }
}
