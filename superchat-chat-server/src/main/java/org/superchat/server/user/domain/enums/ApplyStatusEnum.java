package org.superchat.server.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: 申请状态枚举
 */
@Getter
@AllArgsConstructor
public enum ApplyStatusEnum {

    WAIT_APPROVAL(1, "待审批"),

    AGREE(2, "同意");

    private final Integer code;

    private final String desc;
}
