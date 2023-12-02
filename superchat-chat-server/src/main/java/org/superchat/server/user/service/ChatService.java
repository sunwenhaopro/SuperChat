package org.superchat.server.user.service;

import org.superchat.server.chat.domain.dto.MsgReadInfoDTO;
import org.superchat.server.chat.domain.entity.Message;
import org.superchat.server.chat.domain.vo.request.*;
import org.superchat.server.chat.domain.vo.request.member.MemberReq;
import org.superchat.server.chat.domain.vo.response.ChatMemberStatisticResp;
import org.superchat.server.chat.domain.vo.response.ChatMessageReadResp;
import org.superchat.server.chat.domain.vo.response.ChatMessageResp;
import org.superchat.server.common.domain.vo.response.CursorPageBaseResp;
import org.superchat.server.user.domain.vo.response.ws.ChatMemberResp;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public interface ChatService {
    Long sendMsg(ChatMessageReq req,Long uid);

    CursorPageBaseResp<ChatMessageResp> getMsgPage(ChatMessagePageReq res, @Nullable  Long uid);

    void setMsgMark(Long uid, ChatMessageMarkReq req);

    ChatMessageResp getMsgResp(Long msgId, Long uid);
    ChatMessageResp getMsgResp(Message msg, Long uid);

    CursorPageBaseResp<ChatMessageReadResp> getReadPage(Long uid, ChatMessageReadReq request);

    Collection<MsgReadInfoDTO> getMsgReadInfo(Long uid, ChatMessageReadInfoReq request);

    void msgRead(Long uid, ChatMessageMemberReq request);

    ChatMemberStatisticResp getMemberStatistic();

    void recallMsg(Long uid, ChatMessageBaseReq req);

    CursorPageBaseResp<ChatMemberResp> getMemberPage(List<Long> memberUidList, MemberReq request);
}
