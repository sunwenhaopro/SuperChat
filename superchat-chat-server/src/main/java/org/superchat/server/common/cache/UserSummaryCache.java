package org.superchat.server.common.cache;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.superchat.server.common.constant.RedisKey;
import org.superchat.server.user.dao.UserBackpackDao;
import org.superchat.server.user.domain.entity.*;
import org.superchat.server.user.domain.enums.ItemTypeEnum;
import org.superchat.server.user.dto.SummeryInfoDTO;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class UserSummaryCache extends AbstractRedisStringCache<Long,SummeryInfoDTO>{
    private final UserInfoCache userInfoCache;
    private final UserBackpackDao userBackpackDao;
    private final ItemCache itemCache;
    @Override
    protected String getKey(Long uid) {
        return RedisKey.getKey(RedisKey.USER_SUMMARY_STRING,uid);
    }

    @Override
    protected Long getExpireSeconds() {
        return 10*60L;
    }

    @Override
    protected Map<Long, SummeryInfoDTO> load(List<Long> uidList) {
        //用户基本信息
        Map<Long, User> userMap=userInfoCache.getBatch(uidList);
        //用户徽章信息
        List<ItemConfig> itemConfigList=itemCache.getByType(ItemTypeEnum.BADGE.getType());
        List<Long> itemIds=itemConfigList.stream().map(ItemConfig::getId).collect(Collectors.toList());
        List<UserBackpack> backpacks=userBackpackDao.getByItemIds(uidList,itemIds);
        Map<Long, List<UserBackpack>> userBadgeMap = backpacks.stream().collect(Collectors.groupingBy(UserBackpack::getUid));
        return uidList.stream().map(uid -> {
                    SummeryInfoDTO summeryInfoDTO = new SummeryInfoDTO();
                    User user = userMap.get(uid);
                    if (Objects.isNull(user)) {
                        return null;
                    }
                    List<UserBackpack> userBackpacks = userBadgeMap.getOrDefault(user.getId(), new ArrayList<>());
                    summeryInfoDTO.setUid(user.getId());
                    summeryInfoDTO.setName(user.getName());
                    summeryInfoDTO.setAvatar(user.getAvatar());
                    summeryInfoDTO.setLocPlace(Optional.ofNullable(user.getIpInfo()).map(IpInfo::getUpdateIpDetail).map(IpDetail::getCity).orElse(null));
                    summeryInfoDTO.setWearingItemId(user.getItemId());
                    summeryInfoDTO.setItemIds(userBackpacks.stream().map(UserBackpack::getItemId).collect(Collectors.toList()));
                    return summeryInfoDTO;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(SummeryInfoDTO::getUid, Function.identity()));
    }

}
