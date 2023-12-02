package org.superchat.server.user.service.convert;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;

import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.superchat.server.common.domain.enums.YesOrNoEnum;
import org.superchat.server.user.domain.entity.ItemConfig;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.entity.UserBackpack;
import org.superchat.server.user.domain.vo.response.user.BadgeResp;
import org.superchat.server.user.domain.vo.response.user.UserInfoResp;

import java.util.*;
import java.util.stream.Collectors;

public class UserConvert {
    public static User toUser(String openId)
    {
        User user = new User();
        user.setOpenId(openId);
        return user;
    }
    public static User toUser(Long uid,WxOAuth2UserInfo userInfo)
    {
        User user = new User();
        user.setId(uid);
        user.setAvatar(userInfo.getHeadImgUrl());
        user.setName(userInfo.getNickname());
        user.setSex(userInfo.getSex());
        user.setOpenId(userInfo.getOpenid());
        return user;
    }

    public static UserInfoResp toUserInfoResp(User user, Integer countByValidItemId) {
        UserInfoResp userInfoResp=new UserInfoResp();
        userInfoResp.setId(user.getId());
        userInfoResp.setName(user.getName());
        userInfoResp.setAvatar(user.getAvatar());
        userInfoResp.setSex(user.getSex());
        userInfoResp.setModifyNameChance(countByValidItemId);
        return userInfoResp;
    }

    public static List<BadgeResp> toBadgeRespList(List<ItemConfig> itemConfigList, List<UserBackpack> backpacks, User user) {
        Set<Long> obtainItemSet = backpacks.stream().map(UserBackpack::getItemId).collect(Collectors.toSet());
        return itemConfigList.stream().map(a -> {
                    BadgeResp resp = new BadgeResp();
                    BeanUtil.copyProperties(a, resp);
                    resp.setObtain(obtainItemSet.contains(a.getId()) ? YesOrNoEnum.YES.getStatus() : YesOrNoEnum.NO.getStatus());
                    resp.setWearing(ObjectUtil.equal(a.getId(), user.getItemId()) ? YesOrNoEnum.YES.getStatus() : YesOrNoEnum.NO.getStatus());
                    return resp;
                }).sorted(Comparator.comparing(BadgeResp::getWearing, Comparator.reverseOrder())
                        .thenComparing(BadgeResp::getObtain, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }

    public static User toUpdateUser(User user) {
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setActiveStatus(user.getActiveStatus());
        updateUser.setLastOptTime(user.getLastOptTime());
        updateUser.setIpInfo(user.getIpInfo());
        return updateUser;
    }
    public static User toUpdateIpDetail(User user){
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setIpInfo(user.getIpInfo());
        updateUser.setActiveStatus(user.getActiveStatus());
        updateUser.setLastOptTime(user.getLastOptTime());
        return updateUser;

    }
}
