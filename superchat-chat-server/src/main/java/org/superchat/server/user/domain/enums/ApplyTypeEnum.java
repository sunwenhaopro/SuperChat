package org.superchat.server.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: 申请类型枚举
 */
@Getter
@AllArgsConstructor
public enum ApplyTypeEnum {

    ADD_FRIEND(1, "加好友");


    private final Integer code;

    private final String desc;
}
