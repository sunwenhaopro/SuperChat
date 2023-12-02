package org.superchat.server.user.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.superchat.oss.domain.OssResp;
import org.superchat.server.annotation.ResponseResult;
import org.superchat.server.common.utils.ThreadLocalUtil;
import org.superchat.server.user.domain.vo.request.oss.UploadUrlReq;
import org.superchat.server.user.service.OssService;

import javax.validation.Valid;

@ResponseResult
@AllArgsConstructor
@Slf4j
@Api("对象存储服务接口")
@RequestMapping("/capi/oss")
public class OssController {
    private final OssService ossService;

    @GetMapping("/upload/url")
    @ApiOperation("获取临时上传的链接")
    public OssResp upload(@Valid UploadUrlReq req)
    {
        Long uid= ThreadLocalUtil.getUid();
        return ossService.upload(uid,req);
    }
}
