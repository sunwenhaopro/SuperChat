package org.superchat.server.user.service.serviceImpl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.superchat.server.common.event.UserBlackEvent;
import org.superchat.server.common.utils.ACTrieUtil;
import org.superchat.server.common.utils.AssertUtil;
import org.superchat.server.common.utils.ThreadLocalUtil;
import org.superchat.server.user.dao.BlackDao;
import org.superchat.server.user.dao.ItemConfigDao;
import org.superchat.server.user.dao.UserBackpackDao;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.domain.entity.Black;
import org.superchat.server.user.domain.entity.ItemConfig;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.entity.UserBackpack;
import org.superchat.server.user.domain.enums.*;
import org.superchat.server.user.domain.vo.request.user.*;
import org.superchat.server.user.domain.vo.response.user.BadgeResp;
import org.superchat.server.user.domain.vo.response.user.UserInfoResp;
import org.superchat.server.user.dto.ItemInfoDTO;
import org.superchat.server.user.dto.SummeryInfoDTO;
import org.superchat.server.user.service.RoleService;
import org.superchat.server.user.service.UserService;
import org.superchat.server.common.cache.BlackCache;
import org.superchat.server.common.cache.ItemCache;
import org.superchat.server.common.cache.UserCache;
import org.superchat.server.common.cache.UserSummaryCache;
import org.superchat.server.user.service.convert.UserConvert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserCache userCache;
    private final BlackCache blackCache;
    private final UserDao userDao;
    private final UserBackpackDao userBackpackDao;
    private final ItemConfigDao itemConfigDao;
    private final ItemCache itemCache;
    private final RoleService roleService;
    private final BlackDao blackDao;
    private final UserSummaryCache userSummaryCache;
    private final ApplicationEventPublisher applicationEventPublisher;
    @Override
    public UserInfoResp getUserInfo(Long uid) {
         User user  =  userDao.getById(uid);
         Integer countByValidItemId= userBackpackDao.getCountByValidItemId(uid, ItemEnum.MODIFY_NAME_CARD.getId());
         return UserConvert.toUserInfoResp(user,countByValidItemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void modifyName(ModifyNameReq req) {
        //这里前后名字一样的情况没做判断，前端校验
        //敏感词检测
        String  matchedResult= ACTrieUtil.match(req.getName());
        AssertUtil.equal(matchedResult,req.getName(),"存在敏感词:"+matchedResult);
        Long uid=ThreadLocalUtil.getUid();
        User user=userDao.getByName(req.getName());
        AssertUtil.isEmpty(user,"名字已经被人娶了哦！");
        UserBackpack backpack=userBackpackDao.getFirstValidItem(uid,ItemEnum.MODIFY_NAME_CARD.getId());
        AssertUtil.isNotEmpty(backpack,"改名卡用完了哦！");
        boolean result=userBackpackDao.invalidItem(backpack.getId());
        //改名卡使用成功
        if(result)
        {
            userDao.modifyName(uid, req.getName());
        }
    }

    @Override
    public List<BadgeResp> getBadges(Long uid) {
        List<ItemConfig> itemConfigList=itemCache.getByType(ItemTypeEnum.BADGE.getType());
        List<UserBackpack> backpacks= userBackpackDao.getByItemIds(uid,itemConfigList.stream().map(ItemConfig::getId).collect(Collectors.toList()));
        User user=userDao.getById(uid);
        return UserConvert.toBadgeRespList(itemConfigList,backpacks,user);
    }

    @Override
    public void adornBadge( AdornBadgeReq req) {
        userDao.adornBadge(ThreadLocalUtil.getUid(), req.getBadgeId());
    }

    @Override
    @Transactional
    public void blackUser(BlackReq req) {
        Long uid=ThreadLocalUtil.getUid();
        boolean hasPower=roleService.hasPower(uid, RoleEnum.ADMIN);
        AssertUtil.isTrue(hasPower,"无权限！非法请求！");
        //封禁用户
        Black black=new Black();
        black.setExecutorId(ThreadLocalUtil.getUid());
        black.setTarget(req.getUid().toString());
        black.setType(BlackTypeEnum.UID.getType());
        blackDao.save(black);
        User user=userDao.getById(req.getUid());
        // 封禁ip
        if(Objects.nonNull(user.getIpInfo().getUpdateIp()))
        {
            black.setTarget(user.getIpInfo().getUpdateIp());
            black.setType(BlackTypeEnum.IP.getType());
            blackDao.save(black);
        }
        blackCache.evictSet();
        User updateUser=new User();
        updateUser.setId(user.getId());
        updateUser.setStatus(UserStatusEnum.BAN.getStatus());
        userDao.updateById(updateUser);
        applicationEventPublisher.publishEvent(new UserBlackEvent(this,user));
    }

    @Override
    public List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req) {
        //需要更新的uid
        List<Long> uidList=getNeedSyncUidList(req.getReqList());
        Map<Long,SummeryInfoDTO> batch=userSummaryCache.getBatch(uidList);
        return req.getReqList()
                .stream()
                .map(a -> batch.containsKey(a.getUid()) ? batch.get(a.getUid()) : SummeryInfoDTO.skip(a.getUid()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemInfoDTO> getItemInfo(ItemInfoReq req) {
        return req.getReqList().stream().map(a -> {
            ItemConfig itemConfig = itemCache.getById(a.getItemId());
            if (Objects.nonNull(a.getLastModifyTime()) && a.getLastModifyTime() >= itemConfig.getUpdateTime().getTime()) {
                return ItemInfoDTO.skip(a.getItemId());
            }
            ItemInfoDTO dto = new ItemInfoDTO();
            dto.setItemId(itemConfig.getId());
            dto.setImg(itemConfig.getImg());
            dto.setDescribe(itemConfig.getDescribe());
            return dto;
        }).collect(Collectors.toList());
    }

    private List<Long> getNeedSyncUidList(List<SummeryInfoReq.infoReq> reqList) {

        List<Long> uidList= reqList.stream().map(SummeryInfoReq.infoReq::getUid).collect(Collectors.toList());
        List<Long> modifyTimeList=userCache.getModifyTime(uidList);
        //获取待更新list
        return initNeedSyncUidList(reqList,modifyTimeList);

    }


    /**
     * 根据时间戳判断用户信息是否需要更新
     * @param reqList
     * @param modifyTimeList
     * @return 需要更新的用户list
     */
    private List<Long> initNeedSyncUidList(List<SummeryInfoReq.infoReq> reqList, List<Long> modifyTimeList) {
        List<Long>   needSyncUidList =new ArrayList<>();
        for (int i = 0; i < reqList.size(); i++) {
            SummeryInfoReq.infoReq infoReq = reqList.get(i);
            Long modifyTime = modifyTimeList.get(i);
            if (Objects.isNull(infoReq.getLastModifyTime()) || (Objects.nonNull(modifyTime) && modifyTime > infoReq.getLastModifyTime())) {
                needSyncUidList.add(infoReq.getUid());
            }
        }
        return needSyncUidList;
    }
}
