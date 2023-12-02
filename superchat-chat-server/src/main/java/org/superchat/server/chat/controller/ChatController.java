package org.superchat.server.chat.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.superchat.server.annotation.ResponseResult;
import org.superchat.server.chat.domain.dto.MsgReadInfoDTO;
import org.superchat.server.chat.domain.vo.request.*;
import org.superchat.server.chat.domain.vo.response.ChatMessageReadResp;
import org.superchat.server.chat.domain.vo.response.ChatMessageResp;
import org.superchat.server.common.domain.vo.response.CursorPageBaseResp;
import org.superchat.server.common.utils.ThreadLocalUtil;
import org.superchat.server.user.service.ChatService;
import org.superchat.server.common.cache.BlackCache;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ResponseResult
@Api("聊天室相关接口")
@Slf4j
@RequestMapping("/capi/chat")
@AllArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final BlackCache blackCache;

    @GetMapping("/public/msg/page")
    @ApiOperation("消息列表")
    public CursorPageBaseResp<ChatMessageResp> getMsgPage(@Valid ChatMessagePageReq res)
    {
        CursorPageBaseResp<ChatMessageResp> msgPage=chatService.getMsgPage(res, ThreadLocalUtil.getUid());
        filterBlackMsg(msgPage);
        return msgPage;
    }

    @PostMapping ("/msg")
    @ApiOperation("发送信息")
    public ChatMessageResp sendMsg(@Valid @RequestBody ChatMessageReq req)
    {
        Long msgId= chatService.sendMsg(req,ThreadLocalUtil.getUid());
        return chatService.getMsgResp(msgId,ThreadLocalUtil.getUid());
    }

    @PutMapping("/msg/mark")
    @ApiOperation("消息标记")
    public void setMsgMark(@Valid @RequestBody ChatMessageMarkReq req)
    {
        chatService.setMsgMark(ThreadLocalUtil.getUid(),req);
    }

    @PutMapping("/msg/recall")
    @ApiOperation("撤回消息")
    public void recall(@Valid @RequestBody ChatMessageBaseReq req)
    {
        chatService.recallMsg(ThreadLocalUtil.getUid(),req);
    }

    @GetMapping("/msg/read/page")
    @ApiOperation("消息的已读未读列表")
    public CursorPageBaseResp<ChatMessageReadResp> getReadPage(@Valid ChatMessageReadReq request) {
        Long uid = ThreadLocalUtil.getUid();
        return chatService.getReadPage(uid, request);
    }

    @GetMapping("/msg/read")
    @ApiOperation("获取消息的已读未读总数")
    public Collection<MsgReadInfoDTO> getReadInfo(@Valid ChatMessageReadInfoReq request) {
        Long uid = ThreadLocalUtil.getUid();
        return chatService.getMsgReadInfo(uid, request);
    }

    @PutMapping("/msg/read")
    @ApiOperation("消息阅读上报")
    public void msgRead(@Valid @RequestBody ChatMessageMemberReq request) {
        Long uid = ThreadLocalUtil.getUid();
        chatService.msgRead(uid, request);
    }

    private void filterBlackMsg(CursorPageBaseResp<ChatMessageResp> msgPage) {
        Set<String> blackMembers=getBlackUidSet();
        msgPage.getList().removeIf(a->blackMembers.contains(a.getFromUser().getUid().toString()));
    }

    private Set<String> getBlackUidSet() {
        return Optional.ofNullable(blackCache.getBlackSet()).orElse(new HashSet<>());
    }
}
