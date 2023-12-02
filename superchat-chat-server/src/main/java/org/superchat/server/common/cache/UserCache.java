package org.superchat.server.common.cache;


import cn.hutool.core.collection.CollUtil;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.superchat.server.common.constant.RedisKey;
import org.superchat.server.common.utils.RedisUtils;
import org.superchat.server.user.dao.RoleDao;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.dao.UserRoleDao;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.entity.UserRole;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author CtrlCver
 * @data 2023/12/9
 * @description: 用户相关缓存
 */
@AllArgsConstructor
@Component
public class UserCache {
    private final UserDao userDao;
    private final RoleDao roleDao;
    private final UserSummaryCache userSummaryCache;
    private final UserRoleDao userRoleDao;

    /**
     * @author CtrlCver
     * @data 2023/12/9
     * @description: 在线用户
     */
    public Long getOnlineNum() {
        return RedisUtils.zCard(RedisKey.getKey(RedisKey.ONLINE_UID_ZET));
    }

    /**
     * @author CtrlCver
     * @data 2023/12/9
     * @description: 离线用户
     */
    public Long getOfflineNum() {
        return RedisUtils.zCard(RedisKey.getKey(RedisKey.OFFLINE_UID_ZET));
    }

    /**
     * @author CtrlCver
     * @data 2023/12/9
     * @description: 用户权限
     */
    @Cacheable(cacheNames = "UserRoleCache", key = "#uid")
    public Set<Long> getRoleSet(Long uid) {
        return userRoleDao.listByUid(uid).stream().map(UserRole::getRoleId).collect(Collectors.toSet());
    }


    /**
     * 获取修改时间
     *
     * @param uidList
     */
    public List<Long> getModifyTime(List<Long> uidList) {
        List<String> keys = uidList.stream().map(String::valueOf).collect(Collectors.toList());
        return RedisUtils.mget(keys, Long.class);
    }
    /**
     *@author CtrlCver
     *@data 2023/12/9
     *@description: 移除用户
     */
    public void remove(Long uid)
    {
        String onlineKey = RedisKey.getKey(RedisKey.ONLINE_UID_ZET);
        String offlineKey = RedisKey.getKey(RedisKey.OFFLINE_UID_ZET);
        //移除离线表
        RedisUtils.zRemove(offlineKey, uid);
        //移除上线表
        RedisUtils.zRemove(onlineKey, uid);
    }
    /**
     *@author CtrlCver
     *@data 2023/12/9
     *@description: 用户上线
     */
    public void online(Long uid, Date optTime) {
        String onlineKey = RedisKey.getKey(RedisKey.ONLINE_UID_ZET);
        String offlineKey = RedisKey.getKey(RedisKey.OFFLINE_UID_ZET);
        //移除离线表
        RedisUtils.zRemove(offlineKey, uid);
        //更新上线表
        RedisUtils.zAdd(onlineKey, uid, optTime.getTime());
    }
    /**
     *@author CtrlCver
     *@data 2023/12/9
     *@description: 获取用户上线列表
     */
    public List<Long> getOnlineUidList() {
        String onlineKey = RedisKey.getKey(RedisKey.ONLINE_UID_ZET);
        Set<String> strings = RedisUtils.zAll(onlineKey);
        return strings.stream().map(Long::parseLong).collect(Collectors.toList());
    }
    public boolean isOnline(Long uid) {
        String onlineKey = RedisKey.getKey(RedisKey.ONLINE_UID_ZET);
        return RedisUtils.zIsMember(onlineKey, uid);
    }
    //用户下线
    public void offline(Long uid, Date optTime) {
        String onlineKey = RedisKey.getKey(RedisKey.ONLINE_UID_ZET);
        String offlineKey = RedisKey.getKey(RedisKey.OFFLINE_UID_ZET);
        //移除上线线表
        RedisUtils.zRemove(onlineKey, uid);
        //更新上线表
        RedisUtils.zAdd(offlineKey, uid, optTime.getTime());
    }
    public void delUserInfo(Long uid) {
        String key = RedisKey.getKey(RedisKey.USER_INFO_STRING, uid);
        RedisUtils.del(key);
    }
    public void userInfoChange(Long uid) {
        delUserInfo(uid);
        //删除UserSummaryCache，前端下次懒加载的时候可以获取到最新的数据
        userSummaryCache.delete(uid);
        refreshUserModifyTime(uid);
    }
    public void refreshUserModifyTime(Long uid) {
        String key = RedisKey.getKey(RedisKey.USER_MODIFY_STRING, uid);
        RedisUtils.set(key, new Date().getTime());
    }
    /**
     * 获取用户信息，盘路缓存模式
     */
    public Map<Long, User> getUserInfoBatch(Set<Long> uids) {
        //批量组装key
        List<String> keys = uids.stream().map(a -> RedisKey.getKey(RedisKey.USER_INFO_STRING, a)).collect(Collectors.toList());
        //批量get
        List<User> mget = RedisUtils.mget(keys, User.class);
        Map<Long, User> map = mget.stream().filter(Objects::nonNull).collect(Collectors.toMap(User::getId, Function.identity()));
        //发现差集——还需要load更新的uid
        List<Long> needLoadUidList = uids.stream().filter(a -> !map.containsKey(a)).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(needLoadUidList)) {
            //批量load
            List<User> needLoadUserList = userDao.listByIds(needLoadUidList);
            Map<String, User> redisMap = needLoadUserList.stream().collect(Collectors.toMap(a -> RedisKey.getKey(RedisKey.USER_INFO_STRING, a.getId()), Function.identity()));
            RedisUtils.mset(redisMap, 5 * 60);
            //加载回redis
            map.putAll(needLoadUserList.stream().collect(Collectors.toMap(User::getId, Function.identity())));
        }
        return map;
    }

    public User getUserInfo(Long uid) {
        return getUserInfoBatch(Collections.singleton(uid)).get(uid);
    }
}
