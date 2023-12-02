package org.superchat.server.user.controller;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.superchat.server.annotation.ResponseResult;
import org.superchat.server.common.domain.vo.request.IdReqVO;
import org.superchat.server.common.domain.vo.response.IdRespVO;
import org.superchat.server.common.utils.ThreadLocalUtil;
import org.superchat.server.user.domain.vo.request.user.UserEmojiReq;
import org.superchat.server.user.domain.vo.response.user.UserEmojiResp;
import org.superchat.server.user.service.UserEmojiService;

import javax.validation.Valid;
import java.util.List;

@ResponseResult
@AllArgsConstructor
@Slf4j
@RequestMapping("/capi/user/emoji")
public class UserEmojiController {
    private final UserEmojiService emojiService;
    @GetMapping("/list")
    @ApiOperation("表情包列表")
    public List<UserEmojiResp> getEmojisPage() {
        return emojiService.list(ThreadLocalUtil.getUid());
    }


    @PostMapping()
    @ApiOperation("新增表情包")
    public IdRespVO insertEmojis(@Valid @RequestBody UserEmojiReq req) {
        return emojiService.insert(req, ThreadLocalUtil.getUid());
    }


    @DeleteMapping()
    @ApiOperation("删除表情包")
    public void deleteEmojis(@Valid @RequestBody IdReqVO reqVO) {
        emojiService.remove(reqVO.getId(),ThreadLocalUtil.getUid());
    }
}
