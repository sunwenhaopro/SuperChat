package org.superchat.server.common.event.listener;

import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.superchat.server.common.cache.UserCache;
import org.superchat.server.common.event.UserOfflineEvent;
import org.superchat.server.common.event.UserOnlineEvent;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.service.ChatService;
import org.superchat.server.user.service.IpService;
import org.superchat.server.user.service.WebSocketService;
import org.superchat.server.user.service.convert.UserConvert;
import org.superchat.server.user.service.convert.WSConvert;

@AllArgsConstructor
@Component
public class UserOnlineListener {
    private final UserDao userDao;
    private final UserCache userCache;
    private final IpService ipService;
    private final WebSocketService webSocketService;
    private final ChatService chatService;

    @EventListener(UserOnlineEvent.class)
    public void saveDB(UserOnlineEvent userOnlineEvent)
    {
        User user= userOnlineEvent.getUser();
        User updateUser= UserConvert.toUpdateIpDetail(user);
        userDao.updateById(updateUser);
        ipService.refreshIpDetailAsync(user.getId());
    }

    @Async
    @EventListener(UserOnlineEvent.class)
    public void noticeOtherUser(UserOnlineEvent userOnlineEvent) {
        User user = userOnlineEvent.getUser();
        userCache.online(user.getId(), user.getLastOptTime());
        Long onlineNum=chatService.getMemberStatistic().getOnlineNum();
        //不通知本人
        webSocketService.sendToAllOnline(WSConvert.toOfflineNotifyResp(user,onlineNum),user.getId());
    }
}
