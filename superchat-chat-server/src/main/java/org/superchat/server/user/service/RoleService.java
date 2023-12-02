package org.superchat.server.user.service;

import org.superchat.server.user.domain.enums.RoleEnum;

public interface RoleService {
    boolean hasPower(Long uid, RoleEnum roleEnum);
}
