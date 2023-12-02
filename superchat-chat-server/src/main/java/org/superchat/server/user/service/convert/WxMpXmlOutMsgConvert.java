package org.superchat.server.user.service.convert;

import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;


public class WxMpXmlOutMsgConvert {
    public static WxMpXmlOutMessage toWxMpXmlOutMessage(String content,
                                                        WxMpXmlMessage message,
                                                        WxMpService wxMpService) {
        return WxMpXmlOutMessage.TEXT().content(content)
                .fromUser(message.getToUser()).toUser(message.getFromUser())
                .build();
    }
}
