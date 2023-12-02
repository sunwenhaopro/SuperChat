package org.superchat.server.user.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatusEnum {
    ACTIVE(0,"正常"),
    BAN(1,"封禁"),
    ;
    private final Integer status;
    private final String desc;
}
