package org.superchat.server.common.cache;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.superchat.server.chat.dao.RoomGroupDao;
import org.superchat.server.chat.domain.entity.RoomGroup;
import org.superchat.server.common.constant.RedisKey;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class RoomGroupCache extends AbstractRedisStringCache<Long, RoomGroup> {
    private final RoomGroupDao roomGroupDao;

    @Override
    protected String getKey(Long roomId) {
        return RedisKey.getKey(RedisKey.GROUP_INFO_STRING, roomId);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<Long, RoomGroup> load(List<Long> roomIds) {
        List<RoomGroup> roomGroups = roomGroupDao.listByRoomIds(roomIds);
        return roomGroups.stream().collect(Collectors.toMap(RoomGroup::getRoomId, Function.identity()));
    }
}
