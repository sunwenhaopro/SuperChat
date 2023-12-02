package org.superchat.server.chat.controller;



import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.superchat.server.annotation.ResponseResult;
import org.superchat.server.chat.domain.vo.request.ChatMessageMemberReq;
import org.superchat.server.chat.domain.vo.request.GroupAddReq;
import org.superchat.server.chat.domain.vo.request.admin.AdminAddReq;
import org.superchat.server.chat.domain.vo.request.admin.AdminRevokeReq;
import org.superchat.server.chat.domain.vo.request.member.MemberAddReq;
import org.superchat.server.chat.domain.vo.request.member.MemberDelReq;
import org.superchat.server.chat.domain.vo.request.member.MemberExitReq;
import org.superchat.server.chat.domain.vo.request.member.MemberReq;
import org.superchat.server.chat.domain.vo.response.ChatMemberListResp;
import org.superchat.server.chat.domain.vo.response.MemberResp;
import org.superchat.server.chat.service.RoomAppService;
import org.superchat.server.common.domain.vo.request.IdReqVO;
import org.superchat.server.common.domain.vo.response.CursorPageBaseResp;
import org.superchat.server.common.domain.vo.response.IdRespVO;
import org.superchat.server.common.utils.ThreadLocalUtil;
import org.superchat.server.user.domain.vo.response.ws.ChatMemberResp;
import org.superchat.server.user.service.GroupMemberService;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 房间相关接口
 * </p>
 */
@ResponseResult
@RequestMapping("/capi/room")
@Api(tags = "聊天室相关接口")
@Slf4j
@AllArgsConstructor
public class RoomController {

    private final RoomAppService roomService;
    private final GroupMemberService groupMemberService;

    @GetMapping("/public/group")
    @ApiOperation("群组详情")
    public MemberResp groupDetail(@Valid IdReqVO request) {
        Long uid = ThreadLocalUtil.getUid();
        return roomService.getGroupDetail(uid, request.getId());
    }

    @GetMapping("/public/group/member/page")
    @ApiOperation("群成员列表")
    public CursorPageBaseResp<ChatMemberResp> getMemberPage(@Valid MemberReq request) {
        return roomService.getMemberPage(request);
    }

    @GetMapping("/group/member/list")
    @ApiOperation("房间内的所有群成员列表-@专用")
    public List<ChatMemberListResp> getMemberList(@Valid ChatMessageMemberReq request) {
        return roomService.getMemberList(request);
    }

    @DeleteMapping("/group/member")
    @ApiOperation("移除成员")
    public void delMember(@Valid @RequestBody MemberDelReq request) {
        Long uid = ThreadLocalUtil.getUid();
        roomService.delMember(uid, request);
    }

    @DeleteMapping("/group/member/exit")
    @ApiOperation("退出群聊")
    public void exitGroup(@Valid @RequestBody MemberExitReq request) {
        Long uid = ThreadLocalUtil.getUid();
        groupMemberService.exitGroup(uid, request);
    }

    @PostMapping("/group")
    @ApiOperation("新增群组")
    public IdRespVO addGroup(@Valid @RequestBody GroupAddReq request) {
        Long uid = ThreadLocalUtil.getUid();
        Long roomId = roomService.addGroup(uid, request);
        return IdRespVO.id(roomId);
    }

    @PostMapping("/group/member")
    @ApiOperation("邀请好友")
    public void addMember(@Valid @RequestBody MemberAddReq request) {
        Long uid = ThreadLocalUtil.getUid();
        roomService.addMember(uid, request);
    }

    @PutMapping("/group/admin")
    @ApiOperation("添加管理员")
    public void addAdmin(@Valid @RequestBody AdminAddReq request) {
        Long uid = ThreadLocalUtil.getUid();
        groupMemberService.addAdmin(uid, request);
    }

    @DeleteMapping("/group/admin")
    @ApiOperation("撤销管理员")
    public void revokeAdmin(@Valid @RequestBody AdminRevokeReq request) {
        Long uid =ThreadLocalUtil.getUid();
        groupMemberService.revokeAdmin(uid, request);
    }
}
