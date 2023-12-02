package org.superchat.server.common.consumer;

import lombok.AllArgsConstructor;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.superchat.server.chat.dao.ContactDao;
import org.superchat.server.chat.dao.MessageDao;
import org.superchat.server.chat.dao.RoomDao;
import org.superchat.server.chat.dao.RoomFriendDao;
import org.superchat.server.chat.domain.entity.Message;
import org.superchat.server.chat.domain.entity.Room;
import org.superchat.server.chat.domain.entity.RoomFriend;
import org.superchat.server.chat.domain.enums.RoomTypeEnum;
import org.superchat.server.chat.domain.vo.response.ChatMessageResp;
import org.superchat.server.common.cache.HotRoomCache;
import org.superchat.server.common.cache.RoomCache;
import org.superchat.server.common.constant.MQConstant;
import org.superchat.server.common.domain.dto.MsgSendMessageDTO;
import org.superchat.server.common.service.PushService;
import org.superchat.server.user.service.ChatService;
import org.superchat.server.common.cache.GroupMemberCache;
import org.superchat.server.user.service.convert.WSConvert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


@Component
@AllArgsConstructor
@RocketMQMessageListener(topic = MQConstant.SEND_MSG_TOPIC,consumerGroup = MQConstant.SEND_MSG_GROUP)
public class MsgSendConsumer implements RocketMQListener<MsgSendMessageDTO> {
    private final MessageDao messageDao;
    private final RoomCache roomCache;
    private final ChatService chatService;
    private final RoomDao roomDao;
    private final HotRoomCache hotRoomCache;
    private final GroupMemberCache groupMemberCache;
    private final PushService pushService;
    private final RoomFriendDao roomFriendDao;
    private final ContactDao contactDao;
    @Override
    public void onMessage(MsgSendMessageDTO dto) {
        Message message=messageDao.getById(dto.getMsgId());
        Room room=roomCache.get(message.getRoomId());
        ChatMessageResp messageResp=chatService.getMsgResp(message,null);
        //所有房间更新房间最新消息
        roomDao.refreshActiveTime(room.getId(), dto.getMsgId(),message.getCreateTime());
        roomCache.delete(message.getRoomId());
        //热门群聊推送所有在线的人
        if(room.isHotRoom())
        {
            hotRoomCache.refreshActiveTime(room.getId(), message.getUpdateTime());
            pushService.sendPushMsg(WSConvert.toMsgSend(messageResp));
        }else {
            List<Long> memberUidList=new ArrayList<>();
            //群聊
            if(Objects.equals(room.getType(), RoomTypeEnum.GROUP.getType()))
            {
                memberUidList=groupMemberCache.getMemberUidList(room.getId());

            }else if(Objects.equals(room.getType(),RoomTypeEnum.FRIEND.getType())){ //单聊
                RoomFriend friend=roomFriendDao.getByRoomId(room.getId());
                memberUidList= Arrays.asList(friend.getUid1(), friend.getUid2());
            }
            //更新群成员的会话时间
            contactDao.refreshOrCreateActiveTime(room.getId(), memberUidList, message.getId(), message.getCreateTime());
            pushService.sendPushMsg(WSConvert.toMsgSend(messageResp),memberUidList);
        }
    }
}
