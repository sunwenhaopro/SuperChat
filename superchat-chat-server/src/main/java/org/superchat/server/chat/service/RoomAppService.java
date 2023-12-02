package org.superchat.server.chat.service;

import org.superchat.server.chat.domain.vo.request.ChatMessageMemberReq;
import org.superchat.server.chat.domain.vo.request.GroupAddReq;
import org.superchat.server.chat.domain.vo.request.member.MemberAddReq;
import org.superchat.server.chat.domain.vo.request.member.MemberDelReq;
import org.superchat.server.chat.domain.vo.request.member.MemberReq;
import org.superchat.server.chat.domain.vo.response.ChatMemberListResp;
import org.superchat.server.chat.domain.vo.response.ChatRoomResp;
import org.superchat.server.chat.domain.vo.response.MemberResp;
import org.superchat.server.common.domain.vo.request.CursorPageBaseReq;
import org.superchat.server.common.domain.vo.response.CursorPageBaseResp;
import org.superchat.server.user.domain.vo.response.ws.ChatMemberResp;

import java.util.List;

public interface RoomAppService {
    CursorPageBaseResp<ChatRoomResp> getContactPage(CursorPageBaseReq request, Long uid);

    /**
     * 获取群组信息
     */
    MemberResp getGroupDetail(Long uid, long roomId);

    CursorPageBaseResp<ChatMemberResp> getMemberPage(MemberReq request);

    List<ChatMemberListResp> getMemberList(ChatMessageMemberReq request);

    void delMember(Long uid, MemberDelReq request);

    void addMember(Long uid, MemberAddReq request);

    Long addGroup(Long uid, GroupAddReq request);

    ChatRoomResp getContactDetail(Long uid, Long roomId);

    ChatRoomResp getContactDetailByFriend(Long uid, Long friendUid);
}
