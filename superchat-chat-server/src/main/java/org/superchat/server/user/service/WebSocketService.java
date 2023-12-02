package org.superchat.server.user.service;

import io.netty.channel.Channel;
import org.superchat.server.user.domain.enums.WSBaseResp;
import org.superchat.server.user.domain.vo.response.ws.WSBlack;


public interface WebSocketService {
    void connect(Channel channel);

    void handlerLoginReq(Channel channel);
    void remove(Channel channel);

    void scanLoginSuccess(Long id, Integer code);

    void authorize(Channel channel, String token);

    void broadcastMsg(WSBlack black);

    void sendToUser(Long uid, WSBaseResp<?> wsBaseMsg);

    void sendToAllOnline(WSBaseResp<?> wsBaseMsg, Long skipUid);
}
