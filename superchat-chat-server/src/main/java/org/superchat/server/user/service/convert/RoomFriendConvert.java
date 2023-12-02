package org.superchat.server.user.service.convert;

import org.bouncycastle.asn1.x509.sigi.NameOrPseudonym;
import org.superchat.server.chat.domain.entity.RoomFriend;
import org.superchat.server.chat.domain.enums.RoomTypeEnum;
import org.superchat.server.common.domain.enums.NormalOrNoEnum;

import java.util.List;

public class RoomFriendConvert {
    public static RoomFriend toRoomFriend(String key,Long roomId, List<Long> uidList) {
        RoomFriend roomFriend = new RoomFriend();
        roomFriend.setRoomId(roomId);
        roomFriend.setUid1(uidList.get(0));
        roomFriend.setUid2(uidList.get(1));
        roomFriend.setRoomKey(key);
        roomFriend.setStatus(NormalOrNoEnum.NORMAL.getStatus());
        return roomFriend;
    }
}
