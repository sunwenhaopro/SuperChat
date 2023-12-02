package org.superchat.server.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.superchat.server.annotation.ResponseResult;
import org.superchat.server.common.domain.enums.IdempotentEnum;
import org.superchat.server.common.utils.ThreadLocalUtil;
import org.superchat.server.user.domain.vo.request.user.*;
import org.superchat.server.user.domain.vo.response.user.BadgeResp;
import org.superchat.server.user.domain.vo.response.user.UserInfoResp;
import org.superchat.server.user.dto.ItemInfoDTO;
import org.superchat.server.user.dto.SummeryInfoDTO;
import org.superchat.server.user.service.UserBackpackService;
import org.superchat.server.user.service.UserService;

import javax.validation.Valid;
import java.util.List;


//接口即权限
@ResponseResult
@RequestMapping("/capi/user")
@Api("用户相关接口")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/userInfo")
    @ApiOperation("获取用户个人信息")
    public UserInfoResp getUserInfo() {
        Long uid = ThreadLocalUtil.getUid();
        return userService.getUserInfo(uid);
    }

    @PutMapping("/name")
    @ApiOperation("修改用户名称")
    public void modifyName(@Valid @RequestBody ModifyNameReq req) {
        userService.modifyName(req);
    }
    @GetMapping("/badges")
    @ApiOperation("用户徽章")
    public List<BadgeResp> getBadges()
    {
        return userService.getBadges(ThreadLocalUtil.getUid());
    }
    @PutMapping("/badge")
    @ApiOperation("佩戴徽章")
    public void adornBadge(@Valid @RequestBody AdornBadgeReq req)
    {
        userService.adornBadge(req);
    }

    @PutMapping("/black")
    @ApiOperation("拉黑用户")
    public void addBlack(@Valid @RequestBody BlackReq req)
    {
         userService.blackUser(req);
    }

    @PostMapping("/public/summary/userInfo/batch")
    @ApiOperation("用户聚合信息-返回的代表是要刷新的")
    public List<SummeryInfoDTO> getSummeryUserInfo(@Valid @RequestBody SummeryInfoReq req)
    {
        return userService.getSummeryUserInfo(req);
    }

    @PostMapping("/public/badges/batch")
    @ApiOperation("徽章聚合信息-返回的代表需要刷新的")
    public List<ItemInfoDTO> getItemInfo(@Valid @RequestBody ItemInfoReq req)
    {
        return userService.getItemInfo(req);
    }


}
