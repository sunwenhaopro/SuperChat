package org.superchat.server.user.service.serviceImpl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.superchat.server.user.domain.enums.RoleEnum;
import org.superchat.server.user.service.RoleService;
import org.superchat.server.common.cache.UserCache;

import java.util.Set;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final UserCache userCache;
    @Override
    public boolean hasPower(Long uid, RoleEnum roleEnum) {
        Set<Long> roleSet=userCache.getRoleSet(uid);
        return   isAdmin(roleSet) || roleSet.contains(roleEnum.getId());
    }

    private boolean isAdmin(Set<Long> roleSet)
    {
       return  roleSet.contains(RoleEnum.ADMIN.getId());
    }
}
