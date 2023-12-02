package org.superchat.server.user.service.convert;

import org.superchat.server.user.domain.entity.UserApply;
import org.superchat.server.user.domain.enums.ApplyReadStatusEnum;
import org.superchat.server.user.domain.enums.ApplyStatusEnum;
import org.superchat.server.user.domain.enums.ApplyTypeEnum;
import org.superchat.server.user.domain.vo.request.friend.FriendApplyReq;

public class UserApplyConvert {


    public static UserApply toUserApply(Long uid, FriendApplyReq req) {
        UserApply userApply = new UserApply();
        userApply.setUid(uid);
        userApply.setType(ApplyTypeEnum.ADD_FRIEND.getCode());
        userApply.setTargetId(req.getTargetUid());
        userApply.setMsg(req.getMsg());
        userApply.setStatus(ApplyStatusEnum.WAIT_APPROVAL.getCode());
        userApply.setReadStatus(ApplyReadStatusEnum.UNREAD.getCode());
        return userApply;
    }
}
