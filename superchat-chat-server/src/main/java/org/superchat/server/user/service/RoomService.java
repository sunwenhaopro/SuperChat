package org.superchat.server.user.service;

import org.superchat.server.chat.domain.entity.RoomFriend;
import org.superchat.server.chat.domain.entity.RoomGroup;

import java.util.List;

public interface RoomService {
    RoomFriend createFriendRoom(List<Long> uidList);
    RoomGroup createGroupRoom(Long uid);

    RoomFriend getFriendRoom(Long uid1, Long uid2);
    void disableFriendRoom(List<Long> uidList);

}
