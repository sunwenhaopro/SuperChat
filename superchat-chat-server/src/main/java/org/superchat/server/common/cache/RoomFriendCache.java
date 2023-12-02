package org.superchat.server.common.cache;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.superchat.server.chat.dao.RoomFriendDao;
import org.superchat.server.chat.domain.entity.RoomFriend;
import org.superchat.server.common.constant.RedisKey;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class RoomFriendCache extends AbstractRedisStringCache<Long,RoomFriend> {

    private final RoomFriendDao roomFriendDao;

    @Override
    protected String getKey(Long groupId) {
        return RedisKey.getKey(RedisKey.GROUP_INFO_STRING, groupId);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<Long, RoomFriend> load(List<Long> roomIds) {
        List<RoomFriend> roomGroups = roomFriendDao.listByRoomIds(roomIds);
        return roomGroups.stream().collect(Collectors.toMap(RoomFriend::getRoomId, Function.identity()));
    }
}
