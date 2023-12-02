package org.superchat.server.user.service;

import org.superchat.server.user.domain.vo.request.user.*;
import org.superchat.server.user.domain.vo.response.user.BadgeResp;
import org.superchat.server.user.domain.vo.response.user.UserInfoResp;
import org.superchat.server.user.dto.ItemInfoDTO;
import org.superchat.server.user.dto.SummeryInfoDTO;

import java.util.List;

public interface UserService {

    UserInfoResp getUserInfo(Long uid);

    void modifyName(ModifyNameReq req);

    List<BadgeResp> getBadges(Long uid);

    void adornBadge( AdornBadgeReq req);

    void blackUser(BlackReq req);

    List<SummeryInfoDTO> getSummeryUserInfo(SummeryInfoReq req);

    List<ItemInfoDTO> getItemInfo(ItemInfoReq req);
}
