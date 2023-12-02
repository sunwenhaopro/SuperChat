package org.superchat.server.user.service;

import org.superchat.oss.domain.OssResp;
import org.superchat.server.user.domain.vo.request.oss.UploadUrlReq;

public interface OssService {
    OssResp upload(Long uid, UploadUrlReq req);
}
