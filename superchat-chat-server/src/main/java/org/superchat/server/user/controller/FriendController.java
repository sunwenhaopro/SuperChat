package org.superchat.server.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.superchat.server.annotation.ResponseResult;
import org.superchat.server.common.domain.vo.request.CursorPageBaseReq;
import org.superchat.server.common.domain.vo.request.PageBaseReq;
import org.superchat.server.common.domain.vo.response.CursorPageBaseResp;
import org.superchat.server.common.domain.vo.response.PageBaseResp;
import org.superchat.server.common.utils.ThreadLocalUtil;
import org.superchat.server.user.domain.vo.request.friend.FriendApplyReq;
import org.superchat.server.user.domain.vo.request.friend.FriendApproveReq;
import org.superchat.server.user.domain.vo.request.friend.FriendCheckReq;
import org.superchat.server.user.domain.vo.request.friend.FriendDeleteReq;
import org.superchat.server.user.domain.vo.response.friend.FriendApplyResp;
import org.superchat.server.user.domain.vo.response.friend.FriendCheckResp;
import org.superchat.server.user.domain.vo.response.friend.FriendResp;
import org.superchat.server.user.domain.vo.response.friend.FriendUnreadResp;
import org.superchat.server.user.service.FriendService;

import javax.validation.Valid;

@AllArgsConstructor
@ResponseResult
@RequestMapping("/capi/user/friend")
@Api(tags = "用户好友接口")
public class FriendController {

    private final FriendService friendService;

    @GetMapping("/check")
    @ApiOperation("批量检查好友关系")
    public FriendCheckResp check(@Valid @RequestBody FriendCheckReq req) {
        Long uid = ThreadLocalUtil.getUid();
        return friendService.check(uid, req);
    }

    @PostMapping("/apply")
    @ApiOperation("申请好友")
    public void apply(@Valid @RequestBody FriendApplyReq req)
    {
        Long uid =ThreadLocalUtil.getUid();
        friendService.apply(uid,req);
    }

    @PutMapping("/apply")
    @ApiOperation("同意好友申请")
    public void approve(@Valid @RequestBody FriendApproveReq req)
    {
        Long uid =ThreadLocalUtil.getUid();
        friendService.approve(uid,req);
    }

    @GetMapping("/apply/page")
    @ApiOperation("好友申请列表")
    public PageBaseResp<FriendApplyResp> page(@Valid  PageBaseReq req)
    {
        Long uid=ThreadLocalUtil.getUid();
       return  friendService.pageApplyFriend(uid,req);
    }

    @GetMapping("/apply/unread")
    @ApiOperation("申请未读列表")
    public FriendUnreadResp unRead()
    {
        Long uid=ThreadLocalUtil.getUid();
        return  friendService.unRead(uid);
    }
    @DeleteMapping()
    @ApiOperation("删除好友")
    public void deleteFriend(@Valid @RequestBody FriendDeleteReq req)
    {
        Long uid=ThreadLocalUtil.getUid();
        friendService.deleteFriend(uid,req);
    }
    @GetMapping("/page")
    @ApiOperation("好友列表")
    public CursorPageBaseResp<FriendResp> friendList(@Valid CursorPageBaseReq req)
    {
        Long uid=ThreadLocalUtil.getUid();
        return friendService.friendList(uid,req);
    }
}
