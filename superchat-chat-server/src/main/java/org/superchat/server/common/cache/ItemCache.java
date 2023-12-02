package org.superchat.server.common.cache;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.superchat.server.user.dao.ItemConfigDao;
import org.superchat.server.user.domain.entity.ItemConfig;

import java.util.List;

@Component
@AllArgsConstructor
public class ItemCache {
    private final ItemConfigDao itemConfigDao;

    @Cacheable(key = "'itemsByType'+#itemType", cacheNames = "ItemConfigCache")
    public List<ItemConfig> getByType(Integer itemType) {
        return itemConfigDao.getByType(itemType);
    }

    @Cacheable(key = "'itemsById:'+#itemId", cacheNames = "ItemConfigCache")
    public ItemConfig getById(Long itemId) {
        return itemConfigDao.getById(itemId);
    }
}
