package org.superchat.server.chat.controller;

import cn.hutool.db.ThreadLocalConnection;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.superchat.server.annotation.ResponseResult;
import org.superchat.server.chat.domain.vo.request.ContactFriendReq;
import org.superchat.server.chat.domain.vo.response.ChatRoomResp;
import org.superchat.server.chat.service.RoomAppService;
import org.superchat.server.common.domain.vo.request.CursorPageBaseReq;
import org.superchat.server.common.domain.vo.request.IdReqVO;
import org.superchat.server.common.domain.vo.response.CursorPageBaseResp;
import org.superchat.server.common.utils.ThreadLocalUtil;
import org.superchat.server.user.service.ChatService;

import javax.validation.Valid;

@AllArgsConstructor
@ResponseResult
@RequestMapping("/capi/chat")
@Api("聊天室相关接口")
public class ContactController {
    private final RoomAppService roomService;

    @GetMapping("/public/contact/page")
    @ApiOperation("会话列表")
    public CursorPageBaseResp<ChatRoomResp> getRoomPage(@Valid CursorPageBaseReq request) {
        Long uid = ThreadLocalUtil.getUid();
        return roomService.getContactPage(request, uid);
    }

    @GetMapping("/public/contact/detail")
    @ApiOperation("会话详情")
    public ChatRoomResp getContactDetail(@Valid IdReqVO request) {
        Long uid = ThreadLocalUtil.getUid();
        return roomService.getContactDetail(uid, request.getId());
    }

    @GetMapping("/public/contact/detail/friend")
    @ApiOperation("会话详情(联系人列表发消息用)")
    public ChatRoomResp getContactDetailByFriend(@Valid ContactFriendReq request) {
        Long uid = ThreadLocalUtil.getUid();
        return roomService.getContactDetailByFriend(uid, request.getUid());
    }
}
