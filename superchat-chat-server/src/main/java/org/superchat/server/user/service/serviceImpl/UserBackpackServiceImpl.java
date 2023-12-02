package org.superchat.server.user.service.serviceImpl;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.superchat.server.annotation.RedissonLock;
import org.superchat.server.common.domain.enums.IdempotentEnum;
import org.superchat.server.common.domain.enums.YesOrNoEnum;
import org.superchat.server.user.dao.UserBackpackDao;
import org.superchat.server.user.domain.entity.UserBackpack;
import org.superchat.server.user.service.UserBackpackService;
import org.superchat.server.user.service.convert.UserBackpackConvert;

import java.util.Objects;

@Service
@AllArgsConstructor
public class UserBackpackServiceImpl implements UserBackpackService {
    private final UserBackpackDao userBackpackDao;
    private final ApplicationEventPublisher applicationEventPublisher;

    @RedissonLock(key = "#idempotent",waitTime = 5000)
    public void doAcquireItem(String idempotent, Long uid, Long itemId)
    {
        UserBackpack userBackpack = userBackpackDao.getByIdp(idempotent);
        if (Objects.nonNull(userBackpack)) return;
        UserBackpack insert = UserBackpackConvert.toUserBackpack(uid, itemId, YesOrNoEnum.NO.getStatus(), idempotent);
        applicationEventPublisher.publishEvent(insert);
        userBackpackDao.save(insert);
    }

    @Override
    public void acquireItem(Long uid, Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        String idempotent = getIdempotent(itemId, idempotentEnum, businessId);
        doAcquireItem(idempotent,uid,itemId);
    }

    private String getIdempotent(Long itemId, IdempotentEnum idempotentEnum, String businessId) {
        return String.format("%d_%d_%s", itemId, idempotentEnum.getType(), businessId);
    }
}
