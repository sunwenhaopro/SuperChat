package org.superchat.server.common.cache;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.superchat.server.user.dao.BlackDao;
import org.superchat.server.user.domain.entity.Black;

import java.util.*;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class BlackCache {
    private BlackDao blackDao;

    @Cacheable(cacheNames = "BlackUser",key = "'BlackSet'")
    public Set<String> getBlackSet()
    {
        Set<String> set=blackDao.list().stream().map(Black::getTarget).collect(Collectors.toSet());
        return new HashSet<>(set);
    }
    @CachePut(cacheNames = "BlackUser" ,key = "'BlackSet'")
    public Set<String> updateBlackSet(Black[] blacks)
    {
        Set<String> blackSet=getBlackSet();
        for (Black black : blacks) {
            blackSet.add(black.getTarget());
        }
        return blackSet;
    }
    @CacheEvict(cacheNames = "BlackUser" ,key = "'BlackSet'")
    public void evictSet()
    {}
}
