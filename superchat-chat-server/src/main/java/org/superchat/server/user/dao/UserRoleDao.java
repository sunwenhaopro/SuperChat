package org.superchat.server.user.dao;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.superchat.server.user.domain.entity.UserRole;
import org.superchat.server.user.mapper.UserRoleMapper;


import java.util.List;
import java.util.Objects;

/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: 用户角色关系表 服务实现类
 */
@Service
public class UserRoleDao extends ServiceImpl<UserRoleMapper, UserRole> {
    public List<UserRole> listByUid(Long uid) {
        return lambdaQuery()
                .eq(UserRole::getUid, Objects.requireNonNull(uid))
                .list();
    }
}
