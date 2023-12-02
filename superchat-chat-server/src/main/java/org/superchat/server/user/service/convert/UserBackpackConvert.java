package org.superchat.server.user.service.convert;

import org.superchat.server.user.domain.entity.UserBackpack;

public class UserBackpackConvert {

    public static UserBackpack toUserBackpack(Long uid, Long itemId, Integer status,String idempotent) {
        UserBackpack userBackpack = new UserBackpack();
        userBackpack.setUid(uid);
        userBackpack.setItemId(itemId);
        userBackpack.setStatus(status);
        userBackpack.setIdempotent(idempotent);
        return userBackpack;

    }
}
