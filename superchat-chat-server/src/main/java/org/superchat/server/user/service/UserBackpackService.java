package org.superchat.server.user.service;

import org.superchat.server.common.domain.enums.IdempotentEnum;

public interface UserBackpackService {
    void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum,String businessId);
}
