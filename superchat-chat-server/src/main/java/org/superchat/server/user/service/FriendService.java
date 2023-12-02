package org.superchat.server.user.service;

import org.superchat.server.common.domain.vo.request.CursorPageBaseReq;
import org.superchat.server.common.domain.vo.request.PageBaseReq;
import org.superchat.server.common.domain.vo.response.CursorPageBaseResp;
import org.superchat.server.common.domain.vo.response.PageBaseResp;
import org.superchat.server.user.domain.vo.request.friend.FriendApplyReq;
import org.superchat.server.user.domain.vo.request.friend.FriendApproveReq;
import org.superchat.server.user.domain.vo.request.friend.FriendCheckReq;
import org.superchat.server.user.domain.vo.request.friend.FriendDeleteReq;
import org.superchat.server.user.domain.vo.response.friend.FriendApplyResp;
import org.superchat.server.user.domain.vo.response.friend.FriendCheckResp;
import org.superchat.server.user.domain.vo.response.friend.FriendResp;
import org.superchat.server.user.domain.vo.response.friend.FriendUnreadResp;

public interface FriendService {
    FriendCheckResp check(Long uid, FriendCheckReq req);

    void apply(Long uid, FriendApplyReq req);

    void deleteFriend(Long uid, FriendDeleteReq req);

    void approve(Long uid, FriendApproveReq req);

    PageBaseResp<FriendApplyResp> pageApplyFriend(Long uid, PageBaseReq req);

    FriendUnreadResp unRead(Long uid);

    CursorPageBaseResp<FriendResp> friendList(Long uid, CursorPageBaseReq req);
}
