package org.superchat.server.user.dao;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.superchat.server.user.domain.entity.Black;
import org.superchat.server.user.mapper.BlackMapper;

/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: 黑名单 服务实现类
 */
@Service
public class BlackDao extends ServiceImpl<BlackMapper, Black> {

}
