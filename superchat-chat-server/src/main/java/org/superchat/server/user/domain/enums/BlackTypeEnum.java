package org.superchat.server.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: 物品枚举
 */
@AllArgsConstructor
@Getter
public enum BlackTypeEnum {
    IP(1),
    UID(2),
    ;

    private final Integer type;

}
