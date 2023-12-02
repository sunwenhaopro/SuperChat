package org.superchat.server.user.dao;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.superchat.server.user.domain.entity.ItemConfig;
import org.superchat.server.user.mapper.ItemConfigMapper;

import java.util.List;

/**
 *@author CtrlCver
 *@data 2023/12/3
 *@description: 功能物品配置表 服务实现类
 */
@Service
public class ItemConfigDao extends ServiceImpl<ItemConfigMapper, ItemConfig> {

    public List<ItemConfig> getByType(Integer type) {
        return lambdaQuery().eq(ItemConfig::getType, type).list();
    }
}
