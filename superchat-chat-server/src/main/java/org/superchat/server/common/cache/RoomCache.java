package org.superchat.server.common.cache;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.superchat.server.chat.dao.RoomDao;
import org.superchat.server.chat.domain.entity.Room;
import org.superchat.server.common.constant.RedisKey;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class RoomCache extends AbstractRedisStringCache<Long, Room> {
    private final RoomDao roomDao;

    @Override
    protected String getKey(Long roomId) {

        return RedisKey.getKey(RedisKey.ROOM_INFO_STRING,roomId);
    }

    @Override
    protected Long getExpireSeconds() {

        return 5*60L;
    }

    @Override
    protected Map<Long, Room> load(List<Long> roomIds) {
        List<Room> roomList=roomDao.listByIds(roomIds);
        return roomList.stream().collect(Collectors.toMap(Room::getId, Function.identity()));
    }
}
