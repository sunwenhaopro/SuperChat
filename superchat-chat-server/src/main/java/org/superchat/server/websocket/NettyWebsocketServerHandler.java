package org.superchat.server.websocket;


import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.superchat.server.common.utils.NettyUtil;
import org.superchat.server.user.domain.enums.WSReqTypeEnum;
import org.superchat.server.user.domain.vo.request.ws.WSBaseReq;
import org.superchat.server.user.service.WebSocketService;

import java.util.Objects;


@Slf4j
@Sharable
public class NettyWebsocketServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private WebSocketService webSocketService;
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.webSocketService = getWebSocketService();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object o) {
        if (o instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            System.out.println("握手完成");
            String token=NettyUtil.getAttr(ctx.channel(), NettyUtil.TOKEN);
            if(Objects.nonNull(token))
            {
                webSocketService.authorize(ctx.channel(), token);
            }
        } else if (o instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) o;
            if (event.state() == IdleState.READER_IDLE) {
                System.out.println("用户超时下线");
                offline(ctx.channel());
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        offline(ctx.channel());
        System.out.println("用户主动下线");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        String text = textWebSocketFrame.text();
        WSBaseReq wsBaseReq = JSONUtil.toBean(text, WSBaseReq.class);
        switch (WSReqTypeEnum.of(wsBaseReq.getType())) {
            case HEARTBEAT:
                break;
            case LOGIN:
                webSocketService.handlerLoginReq(channelHandlerContext.channel());
                break;
            case AUTHORIZE:
                webSocketService.authorize(channelHandlerContext.channel(),wsBaseReq.getData());
                break;

        }
    }

    public void offline(Channel channel) {
        webSocketService.remove(channel);
        channel.close();
    }

    public WebSocketService getWebSocketService() {
        return SpringUtil.getBean(WebSocketService.class);
    }
}
