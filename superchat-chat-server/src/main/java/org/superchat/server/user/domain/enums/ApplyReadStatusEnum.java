package org.superchat.server.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: 申请阅读状态枚举
 */
@Getter
@AllArgsConstructor
public enum ApplyReadStatusEnum {

    UNREAD(1, "未读"),

    READ(2, "已读");

    private final Integer code;

    private final String desc;
}
