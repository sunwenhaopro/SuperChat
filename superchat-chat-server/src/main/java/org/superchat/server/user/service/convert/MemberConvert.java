package org.superchat.server.user.service.convert;

import org.springframework.beans.BeanUtils;
import org.superchat.server.chat.domain.entity.GroupMember;
import org.superchat.server.chat.domain.enums.GroupRoleEnum;
import org.superchat.server.chat.domain.vo.response.ChatMemberListResp;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.enums.WSBaseResp;
import org.superchat.server.user.domain.enums.WSRespTypeEnum;
import org.superchat.server.user.domain.vo.response.ws.ChatMemberResp;
import org.superchat.server.user.domain.vo.response.ws.WSMemberChange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.superchat.server.user.domain.vo.response.ws.WSMemberChange.CHANGE_TYPE_ADD;
import static org.superchat.server.user.domain.vo.response.ws.WSMemberChange.CHANGE_TYPE_REMOVE;

public class MemberConvert {
    public static List<ChatMemberListResp> toMemberList(List<User> memberList) {
        return memberList.stream()
                .map(a -> {
                    ChatMemberListResp resp = new ChatMemberListResp();
                    BeanUtils.copyProperties(a, resp);
                    resp.setUid(a.getId());
                    return resp;
                }).collect(Collectors.toList());
    }
    public static List<ChatMemberListResp> toMemberList(Map<Long, User> batch) {
        return toMemberList(new ArrayList<>(batch.values()));
    }

    public static WSBaseResp<WSMemberChange> toMemberRemoveWS(Long roomId, Long uid) {
        WSBaseResp<WSMemberChange> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WSRespTypeEnum.MEMBER_CHANGE.getType());
        WSMemberChange wsMemberChange = new WSMemberChange();
        wsMemberChange.setUid(uid);
        wsMemberChange.setRoomId(roomId);
        wsMemberChange.setChangeType(CHANGE_TYPE_REMOVE);
        wsBaseResp.setData(wsMemberChange);
        return wsBaseResp;
    }

    public static List<GroupMember> toMemberAdd(Long id, List<Long> waitAddUidList) {
        return waitAddUidList.stream().map(a -> {
            GroupMember member = new GroupMember();
            member.setGroupId(id);
            member.setUid(a);
            member.setRole(GroupRoleEnum.MEMBER.getType());
            return member;
        }).collect(Collectors.toList());
    }

    public static Collection<? extends ChatMemberResp> toMember(List<User> list) {
        return list.stream().map(a -> {
            ChatMemberResp resp = new ChatMemberResp();
            resp.setActiveStatus(a.getActiveStatus());
            resp.setLastOptTime(a.getLastOptTime());
            resp.setUid(a.getId());
            return resp;
        }).collect(Collectors.toList());
    }

    public static WSBaseResp<WSMemberChange> toMemberAddWS(Long roomId, User user) {
        WSBaseResp<WSMemberChange> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WSRespTypeEnum.MEMBER_CHANGE.getType());
        WSMemberChange wsMemberChange = new WSMemberChange();
        wsMemberChange.setActiveStatus(user.getActiveStatus());
        wsMemberChange.setLastOptTime(user.getLastOptTime());
        wsMemberChange.setUid(user.getId());
        wsMemberChange.setRoomId(roomId);
        wsMemberChange.setChangeType(CHANGE_TYPE_ADD);
        wsBaseResp.setData(wsMemberChange);
        return wsBaseResp;
    }
}
