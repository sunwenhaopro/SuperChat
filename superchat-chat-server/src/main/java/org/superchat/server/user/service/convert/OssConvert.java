package org.superchat.server.user.service.convert;

import org.superchat.oss.domain.OssReq;

public class OssConvert {
    public static OssReq toOssReq(String fileName, String path, Long uid) {
        OssReq ossReq = new OssReq();
        ossReq.setFilePath(path);
        ossReq.setFileName(fileName);
        ossReq.setUid(uid);
        return ossReq;
    }
}
