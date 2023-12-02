package org.superchat.server.websocket;



import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

import org.apache.commons.lang3.StringUtils;
import org.superchat.server.common.utils.NettyUtil;

import java.net.InetSocketAddress;


public class MyHeaderAnalyzeHandler  extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)  {
        if(msg instanceof FullHttpRequest)
        {
            FullHttpRequest request=(FullHttpRequest) msg;
            String uri=request.uri();
            if(!"/".equals(uri))
            {
               String token=uri.split("=")[1];
               if(!token.isBlank())
               {
                   NettyUtil.setAttr(ctx.channel(),NettyUtil.TOKEN,token);
               }
            }
            String ip = request.headers().get("X-Real-IP");
            if(StringUtils.isBlank(ip))
            {
               ip=((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
            }
            NettyUtil.setAttr(ctx.channel(),NettyUtil.IP,ip);
        }
        ctx.fireChannelRead(msg);
    }
}
