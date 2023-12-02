package org.superchat.server.user.service.convert;
import com.google.common.collect.Lists;

import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.entity.UserApply;
import org.superchat.server.user.domain.vo.response.friend.FriendApplyResp;
import org.superchat.server.user.domain.vo.response.friend.FriendResp;

import java.util.List;
import java.util.stream.Collectors;

public class FriendConvert {
    public static List<FriendApplyResp> toFriendApplyList(List<UserApply> records) {
        return records.stream().map(FriendConvert::toFriendApplyResp).collect(Collectors.toList());
    }
    public static FriendApplyResp toFriendApplyResp(UserApply userApply)
    {
        FriendApplyResp friendApplyResp = new FriendApplyResp();
        friendApplyResp.setApplyId(userApply.getId());
        friendApplyResp.setUid(userApply.getUid());
        friendApplyResp.setType(userApply.getType());
        friendApplyResp.setMsg(userApply.getMsg());
        friendApplyResp.setStatus(userApply.getStatus());
        return friendApplyResp;
    }

    public static List<FriendResp> toFriendRespList(List<User> userList) {
        return userList.stream().map(FriendConvert::toFriendResp).collect(Collectors.toList());
    }
    public static FriendResp toFriendResp(User user)
    {
        return FriendResp.builder()
        		.uid(user.getId())
        		.activeStatus(user.getActiveStatus())
        		.build();

    }
}
