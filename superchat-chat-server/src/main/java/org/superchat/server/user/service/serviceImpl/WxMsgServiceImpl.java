package org.superchat.server.user.service.serviceImpl;


import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.superchat.server.common.event.UserRegisterEvent;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.service.WebSocketService;
import org.superchat.server.user.service.WxMsgService;
import org.superchat.server.user.service.convert.UserConvert;
import org.superchat.server.user.service.convert.WxMpXmlOutMsgConvert;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class WxMsgServiceImpl implements WxMsgService {

    private static final ConcurrentHashMap<String,Integer> TEM_AUTH_MAP = new ConcurrentHashMap<>();
    private static final String URL = "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_userinfo&state=STATE#wechat_redirect";
    @Value("${wx.mp.callback}")
    private String callback;
    @Resource
    private UserDao userDao;
    @Resource
    private WebSocketService webSocketService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public WxMpXmlOutMessage scan(WxMpService wxMpService, WxMpXmlMessage wxMpXmlMessage) {
        String openid = wxMpXmlMessage.getFromUser();
        Integer code = Integer.parseInt(wxMpXmlMessage.getEventKey().replace("qrscene_", ""));
        User user=userDao.getByOpenId(openid);
        // 用户不存在--> 用户注册
        if(Objects.isNull(user))
        {
            user= UserConvert.toUser(openid);
            userDao.save(user);
            applicationEventPublisher.publishEvent(new UserRegisterEvent(this,user));
        }
        TEM_AUTH_MAP.put(openid,code);
        String skipUrl = String.format(URL, wxMpService.getWxMpConfigStorage().getAppId(), URLEncoder.encode(callback + "/wx/portal/public/callBack"));
        WxMpXmlOutMessage.TEXT().build();
        return WxMpXmlOutMsgConvert.toWxMpXmlOutMessage("请点击链接授权：<a href=\"" + skipUrl + "\">登录</a>", wxMpXmlMessage, wxMpService);
    }

    @Override
    public void anthorize(WxOAuth2UserInfo userInfo) {
        User user = userDao.getByOpenId(userInfo.getOpenid());
        //第一次登录补充信息
        if(StringUtils.isBlank(user.getAvatar()))
        {
            user=UserConvert.toUser(user.getId(),userInfo);
            userDao.updateById(user);
        }
        Integer code = TEM_AUTH_MAP.remove(userInfo.getOpenid());
        webSocketService.scanLoginSuccess(user.getId(),code);
     }
}
