package org.superchat.server.user.dao;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.superchat.server.user.domain.entity.UserEmoji;
import org.superchat.server.user.mapper.UserEmojiMapper;

import java.util.List;

/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: 用户表情包 服务实现类
 */
@Service
public class UserEmojiDao extends ServiceImpl<UserEmojiMapper, UserEmoji> {

    public List<UserEmoji> listByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid).list();
    }

    public int countByUid(Long uid) {
        return lambdaQuery().eq(UserEmoji::getUid, uid).count();
    }
}
