package org.superchat.server.user.service.convert;

import org.superchat.server.chat.domain.entity.Contact;
import org.superchat.server.chat.domain.entity.RoomFriend;
import org.superchat.server.chat.domain.entity.RoomGroup;
import org.superchat.server.common.domain.enums.NormalOrNoEnum;
import org.superchat.server.user.domain.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ChatConvert {
    public static final String SEPARATOR = ",";
    public static String generateRoomKey(List<Long> uidList) {
        return uidList.stream().map(String::valueOf).collect(Collectors.joining(SEPARATOR));
    }

    public static Contact toContact(Long uid, Long roomId) {
        Contact contact = new Contact();
        contact.setUid(uid);
        contact.setRoomId(roomId);
        return contact;

    }


    public static Set<Long> toFriendUidSet(Collection<RoomFriend> values, Long uid) {
        return values.stream()
                .map(a -> toFriendUid(a, uid))
                .collect(Collectors.toSet());
    }

    /**
     * 获取好友uid
     */
    public static Long toFriendUid(RoomFriend roomFriend, Long uid) {
        return Objects.equals(uid, roomFriend.getUid1()) ? roomFriend.getUid2() : roomFriend.getUid1();
    }

    public static RoomGroup toGroupRoom(User user, Long id) {
        RoomGroup roomGroup = new RoomGroup();
        roomGroup.setRoomId(id);
        roomGroup.setName(user.getName()+"的群主");
        roomGroup.setAvatar(user.getAvatar());
        return roomGroup;
    }
    public static RoomFriend toFriendRoom(Long roomId, List<Long> uidList) {
        RoomFriend roomFriend = new RoomFriend();
        roomFriend.setRoomId(roomId);
        roomFriend.setUid1(uidList.get(0));
        roomFriend.setUid2(uidList.get(1));
        roomFriend.setRoomKey(generateRoomKey(uidList));
        roomFriend.setStatus(NormalOrNoEnum.NORMAL.getStatus());
        return roomFriend;

    }
}
