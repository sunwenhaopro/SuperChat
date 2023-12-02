package org.superchat.server.user.service.convert;

import org.superchat.server.chat.domain.entity.Contact;
import org.superchat.server.chat.domain.entity.GroupMember;
import org.superchat.server.chat.domain.entity.Room;
import org.superchat.server.chat.domain.entity.RoomGroup;
import org.superchat.server.chat.domain.enums.GroupRoleEnum;
import org.superchat.server.chat.domain.enums.HotFlagEnum;
import org.superchat.server.chat.domain.enums.MessageTypeEnum;
import org.superchat.server.chat.domain.vo.request.ChatMessageReq;
import org.superchat.server.chat.domain.vo.response.ChatMessageReadResp;
import org.superchat.server.user.domain.entity.User;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoomConvert {
    public static Room toRoom(Integer type) {
        Room room = new Room();
        room.setType(type);
        room.setHotFlag(HotFlagEnum.NOT.getType());
        return room;
    }

    public static List<ChatMessageReadResp> toReadResp(List<Contact> list) {
        return list.stream().map(contact -> new ChatMessageReadResp(contact.getUid())).collect(Collectors.toList());

    }

    public static List<GroupMember> toGroupMemberBatch(List<Long> uidList, Long id) {
        return uidList.stream()
                .distinct()
                .map(uid -> {
                    GroupMember member = new GroupMember();
                    member.setRole(GroupRoleEnum.MEMBER.getType());
                    member.setUid(uid);
                    member.setGroupId(id);
                    return member;
                }).collect(Collectors.toList());
    }

    public static ChatMessageReq toGroupAddMessage(RoomGroup groupRoom, User inviter, Map<Long, User> member) {
        ChatMessageReq chatMessageReq = new ChatMessageReq();
        chatMessageReq.setRoomId(groupRoom.getRoomId());
        chatMessageReq.setMsgType(MessageTypeEnum.SYSTEM.getType());
        StringBuilder sb = new StringBuilder();
        sb.append("\"")
                .append(inviter.getName())
                .append("\"")
                .append("邀请")
                .append(member.values().stream().map(u -> "\"" + u.getName() + "\"").collect(Collectors.joining(",")))
                .append("加入群聊");
        chatMessageReq.setBody(sb.toString());
        return chatMessageReq;
    }
}
