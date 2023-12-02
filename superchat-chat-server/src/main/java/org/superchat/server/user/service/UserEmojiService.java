package org.superchat.server.user.service;

import org.superchat.server.common.domain.vo.response.IdRespVO;
import org.superchat.server.user.domain.vo.request.user.UserEmojiReq;
import org.superchat.server.user.domain.vo.response.user.UserEmojiResp;

import java.util.List;

public interface UserEmojiService {
    List<UserEmojiResp> list(Long uid);

    IdRespVO insert(UserEmojiReq req, Long uid);
    void remove(Long id, Long uid);
}
