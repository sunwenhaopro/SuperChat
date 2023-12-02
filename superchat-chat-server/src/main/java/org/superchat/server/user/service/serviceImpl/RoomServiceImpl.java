package org.superchat.server.user.service.serviceImpl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.superchat.server.annotation.RedissonLock;
import org.superchat.server.chat.dao.GroupMemberDao;
import org.superchat.server.chat.dao.RoomDao;
import org.superchat.server.chat.dao.RoomFriendDao;
import org.superchat.server.chat.dao.RoomGroupDao;
import org.superchat.server.chat.domain.entity.GroupMember;
import org.superchat.server.chat.domain.entity.Room;
import org.superchat.server.chat.domain.entity.RoomFriend;
import org.superchat.server.chat.domain.entity.RoomGroup;
import org.superchat.server.chat.domain.enums.GroupRoleEnum;
import org.superchat.server.chat.domain.enums.RoomTypeEnum;
import org.superchat.server.common.domain.enums.NormalOrNoEnum;
import org.superchat.server.common.utils.AssertUtil;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.service.RoomService;
import org.superchat.server.common.cache.UserInfoCache;
import org.superchat.server.user.service.convert.ChatConvert;
import org.superchat.server.user.service.convert.RoomConvert;
import org.superchat.server.user.service.convert.RoomFriendConvert;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

// toList
@Service
@AllArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {


    private final RoomFriendDao roomFriendDao;
    private final RoomDao roomDao;
    private final UserInfoCache userInfoCache;
    private final GroupMemberDao groupMemberDao;
    private final RoomGroupDao roomGroupDao;

    @Override
    public void disableFriendRoom(List<Long> uidList) {
        AssertUtil.isNotEmpty(uidList, "房间创建失败，好友数量不对");
        AssertUtil.equal(uidList.size(), 2, "房间创建失败，好友数量不对");
        String key = ChatConvert.generateRoomKey(uidList);
        roomFriendDao.disableRoom(key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RoomFriend createFriendRoom(List<Long> uidList) {
        AssertUtil.isNotEmpty(uidList, "房间创建失败，好友数量不对");
        AssertUtil.equal(uidList.size(), 2, "房间创建失败，好友数量不对");
        String key = ChatConvert.generateRoomKey(uidList);
        RoomFriend roomFriend = roomFriendDao.getByKey(key);
        if (Objects.nonNull(roomFriend)) {
            restoreIfNeed(roomFriend);
        } else {
            Room room = createRoom(RoomTypeEnum.FRIEND.getType());
            roomFriend = createFriendRoom(key,room.getId(), uidList);
        }
        return roomFriend;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @RedissonLock(key = "#uid")
    public RoomGroup createGroupRoom(Long uid) {
        List<GroupMember> selfGroup = groupMemberDao.getSelfGroup(uid);
        AssertUtil.isEmpty(selfGroup, "每个人只能创建一个群");
        User user = userInfoCache.get(uid);
        Room room = createRoom(RoomTypeEnum.GROUP.getType());
        //插入群
        RoomGroup roomGroup = ChatConvert.toGroupRoom(user, room.getId());
        roomGroupDao.save(roomGroup);
        //插入群主
        GroupMember leader = GroupMember.builder()
                .role(GroupRoleEnum.LEADER.getType())
                .groupId(roomGroup.getId())
                .uid(uid)
                .build();
        groupMemberDao.save(leader);
        return roomGroup;
    }

    @Override
    public RoomFriend getFriendRoom(Long uid1, Long uid2) {
        String key = ChatConvert.generateRoomKey(Arrays.asList(uid1, uid2));
        return roomFriendDao.getByKey(key);
    }

    private void restoreIfNeed(RoomFriend roomFriend) {
        if(Objects.equals(roomFriend.getStatus(), NormalOrNoEnum.NOT_NORMAL.getStatus()))
        {
        roomFriendDao.restoreRoom(roomFriend.getId());
        }
    }
    private RoomFriend createFriendRoom(String key,Long id,List<Long> uidList)
    {
        RoomFriend roomFriend= RoomFriendConvert.toRoomFriend(key,id,uidList);
        roomFriendDao.save(roomFriend);
        return roomFriend;
    }
    private Room createRoom(Integer type)
    {
        Room insert= RoomConvert.toRoom(type);
        roomDao.save(insert);
        return insert;
    }

}
