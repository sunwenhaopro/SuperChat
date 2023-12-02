package org.superchat.server.common.cache;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.superchat.server.common.constant.RedisKey;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.domain.entity.User;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserInfoCache extends AbstractRedisStringCache<Long, User>{

    private final UserDao userDao;

    @Override
    protected String getKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_INFO_STRING, uid);
    }

    @Override
    protected Long getExpireSeconds() {
        return 5 * 60L;
    }

    @Override
    protected Map<Long, User> load(List<Long> uidList) {
        List<User> needLoadUserList = userDao.listByIds(uidList);
        return needLoadUserList.stream().collect(Collectors.toMap(User::getId, Function.identity()));
    }
}
