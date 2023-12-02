package org.superchat.server.user.service.serviceImpl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.superchat.oss.domain.MinIOTemplate;
import org.superchat.oss.domain.OssReq;
import org.superchat.oss.domain.OssResp;
import org.superchat.server.common.utils.AssertUtil;
import org.superchat.server.user.domain.enums.OssSceneEnum;
import org.superchat.server.user.domain.vo.request.oss.UploadUrlReq;
import org.superchat.server.user.service.OssService;
import org.superchat.server.user.service.convert.OssConvert;

@Service
@AllArgsConstructor
public class OssServiceImpl implements OssService {
    private final MinIOTemplate minIOTemplate;

    @Override
    public OssResp upload(Long uid, UploadUrlReq req) {
        OssSceneEnum sceneEnum=OssSceneEnum.of(req.getScene());
        AssertUtil.isNotEmpty(sceneEnum,"非该场景");
        OssReq ossReq= OssConvert.toOssReq(req.getFileName(),sceneEnum.getPath(),uid);
        return minIOTemplate.getPreSignedObjectUrl(ossReq);
    }
}
