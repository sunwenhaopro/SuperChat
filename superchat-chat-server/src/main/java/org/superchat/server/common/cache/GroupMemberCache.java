package org.superchat.server.common.cache;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.superchat.server.chat.dao.GroupMemberDao;
import org.superchat.server.chat.dao.MessageDao;
import org.superchat.server.chat.dao.RoomGroupDao;
import org.superchat.server.chat.domain.entity.GroupMember;
import org.superchat.server.chat.domain.entity.RoomGroup;

import java.util.List;
import java.util.Objects;

/**
 *@author CtrlCver
 *@data 2023/12/9
 *@description: 成员相关缓存
 */
@Component
@AllArgsConstructor
public class GroupMemberCache {
    private final MessageDao messageDao;
    private final RoomGroupDao roomGroupDao;
    private final GroupMemberDao groupMemberDao;
    @Cacheable(cacheNames = "SuperChat",key = "'groupMember'+#roomId")
    public List<Long> getMemberUidList(Long roomId) {
        RoomGroup roomGroup=roomGroupDao.getByRoomId(roomId);
        if(Objects.isNull(roomGroup))
        {
            return null;
        }
        return groupMemberDao.getMemberUidList(roomGroup.getId());
    }
    @CacheEvict(cacheNames = "SuperChat",key = "'groupMember'+#roomId")
    public void evict(Long roomId)
    {}


}
