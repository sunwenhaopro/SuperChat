package org.superchat.server.common.event.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.superchat.server.chat.dao.GroupMemberDao;
import org.superchat.server.chat.domain.entity.GroupMember;
import org.superchat.server.chat.domain.entity.RoomGroup;
import org.superchat.server.chat.domain.vo.request.ChatMessageReq;
import org.superchat.server.common.cache.MsgCache;
import org.superchat.server.common.event.GroupMemberAddEvent;
import org.superchat.server.common.service.PushService;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.enums.WSBaseResp;
import org.superchat.server.user.domain.vo.response.ws.WSMemberChange;
import org.superchat.server.user.service.ChatService;
import org.superchat.server.user.service.WebSocketService;
import org.superchat.server.common.cache.GroupMemberCache;
import org.superchat.server.common.cache.UserInfoCache;
import org.superchat.server.user.service.convert.MemberConvert;
import org.superchat.server.user.service.convert.RoomConvert;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupMemberAddListener {
    @Autowired
    private WebSocketService webSocketService;
    @Autowired
    private ChatService chatService;
    @Autowired
    private MsgCache msgCache;
    @Autowired
    private UserInfoCache userInfoCache;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private GroupMemberDao groupMemberDao;
    @Autowired
    private UserDao userDao;
    @Autowired
    private GroupMemberCache groupMemberCache;
    @Autowired
    private PushService pushService;


    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendAddMsg(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getGroupMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        Long inviteUid = event.getUid();
        User user = userInfoCache.get(inviteUid);
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        ChatMessageReq chatMessageReq = RoomConvert.toGroupAddMessage(roomGroup, user, userInfoCache.getBatch(uidList));
        chatService.sendMsg(chatMessageReq, User.UID_SYSTEM);
    }

    @Async
    @TransactionalEventListener(classes = GroupMemberAddEvent.class, fallbackExecution = true)
    public void sendChangePush(GroupMemberAddEvent event) {
        List<GroupMember> memberList = event.getGroupMemberList();
        RoomGroup roomGroup = event.getRoomGroup();
        List<Long> memberUidList = groupMemberCache.getMemberUidList(roomGroup.getRoomId());
        List<Long> uidList = memberList.stream().map(GroupMember::getUid).collect(Collectors.toList());
        List<User> users = userDao.listByIds(uidList);
        users.forEach(user -> {
            WSBaseResp<WSMemberChange> ws = MemberConvert.toMemberAddWS(roomGroup.getRoomId(), user);
            pushService.sendPushMsg(ws, memberUidList);
        });
        //移除缓存
        groupMemberCache.evict(roomGroup.getRoomId());
    }

}
