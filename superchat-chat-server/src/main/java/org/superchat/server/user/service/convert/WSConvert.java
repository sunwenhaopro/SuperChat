package org.superchat.server.user.service.convert;

import cn.hutool.core.bean.BeanUtil;
import org.springframework.beans.BeanUtils;
import org.superchat.server.chat.domain.dto.ChatMessageMarkDTO;
import org.superchat.server.chat.domain.dto.ChatMsgRecallDTO;
import org.superchat.server.chat.domain.vo.response.ChatMessageResp;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.enums.ChatActiveStatusEnum;
import org.superchat.server.user.domain.enums.WSBaseResp;
import org.superchat.server.user.domain.enums.WSRespTypeEnum;
import org.superchat.server.user.domain.vo.response.ws.*;

import java.util.Collections;

public class WSConvert {
    public static WSBaseResp<WSLoginUrl> toWSBaseResp(String url) {
        WSBaseResp<WSLoginUrl> resp = new WSBaseResp<>();
        resp.setType(1);
        resp.setData(new WSLoginUrl(url));
        return resp;
    }

    public static WSBaseResp<WSLoginSuccess> toLoginSuccessResp(User user, String token, boolean hasPower) {
        WSLoginSuccess wsLoginSuccess = new WSLoginSuccess();
        wsLoginSuccess.setAvatar(user.getAvatar());
        wsLoginSuccess.setUid(user.getId());
        wsLoginSuccess.setName(user.getName());
        wsLoginSuccess.setToken(token);
        wsLoginSuccess.setPower(hasPower ? 1 : 0);
        WSBaseResp<WSLoginSuccess> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.LOGIN_SUCCESS.getType());
        resp.setData(wsLoginSuccess);
        return resp;
    }

    public static WSBaseResp<WSLoginSuccess> toInvalidToken() {
        WSBaseResp<WSLoginSuccess> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.INVALIDATE_TOKEN.getType());
        return resp;
    }

    public static WSBaseResp<ChatMessageResp> toMsgSend(ChatMessageResp messageResp) {
        WSBaseResp wSBaseResp = new WSBaseResp();
        wSBaseResp.setType(WSRespTypeEnum.MESSAGE.getType());
        wSBaseResp.setData(messageResp);
        return wSBaseResp;

    }

    public static WSBaseResp<WSOnlineOfflineNotify> toOfflineNotifyResp(User user, Long onlineNum) {
        WSBaseResp<WSOnlineOfflineNotify> resp = new WSBaseResp<>();
        resp.setType(WSRespTypeEnum.ONLINE_OFFLINE_NOTIFY.getType());
        WSOnlineOfflineNotify notify = new WSOnlineOfflineNotify();
        notify.setChangeList(Collections.singletonList(buildOnlineInfo(user)));
        notify.setOnlineNum(onlineNum);
        resp.setData(notify);
        return resp;
    }

    private static ChatMemberResp buildOnlineInfo(User user) {
        ChatMemberResp info = new ChatMemberResp();
        BeanUtil.copyProperties(user, info);
        info.setUid(user.getId());
        info.setActiveStatus(ChatActiveStatusEnum.ONLINE.getStatus());
        info.setLastOptTime(user.getLastOptTime());
        return info;
    }

    public static WSBaseResp<ChatMsgRecallDTO> toMsgRecall(ChatMsgRecallDTO chatMsgRecallDTO) {
        WSBaseResp<ChatMsgRecallDTO> wSBaseResp = new WSBaseResp();
        WSMsgRecall recall=new WSMsgRecall();
        BeanUtil.copyProperties(chatMsgRecallDTO,recall);
        wSBaseResp.setType(WSRespTypeEnum.RECALL.getType());
        wSBaseResp.setData(recall);
        return wSBaseResp;
    }

    public static WSBaseResp<WSMsgMark> toMsgMarkSend(ChatMessageMarkDTO dto, Integer markCount) {
        WSMsgMark.WSMsgMarkItem item = new WSMsgMark.WSMsgMarkItem();
        BeanUtils.copyProperties(dto, item);
        item.setMarkCount(markCount);
        WSBaseResp<WSMsgMark> wsBaseResp = new WSBaseResp<>();
        wsBaseResp.setType(WSRespTypeEnum.MARK.getType());
        WSMsgMark mark = new WSMsgMark();
        mark.setMarkList(Collections.singletonList(item));
        wsBaseResp.setData(mark);
        return wsBaseResp;
    }
}
