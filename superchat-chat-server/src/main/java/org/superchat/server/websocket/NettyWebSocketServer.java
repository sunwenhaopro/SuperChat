package org.superchat.server.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Slf4j
@Configuration
public class NettyWebSocketServer {
    private static final Integer WEBSOCKET_PORT = 8090;
    private static final NettyWebsocketServerHandler WEBSOCKET_HANDLER = new NettyWebsocketServerHandler();

    private EventLoopGroup boseGroup = new NioEventLoopGroup(1);
    private EventLoopGroup employerGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors());

    @PostConstruct
    public void start() throws InterruptedException {
           run();
    }

    @PreDestroy
    public void destroy() {
        Future<?> future1 = boseGroup.shutdownGracefully();
        Future<?> future2 = employerGroup.shutdownGracefully();
        future1.syncUninterruptibly();
        future2.syncUninterruptibly();
        log.info("WebSocket服务关闭");
    }

    public void run() throws InterruptedException {
        ServerBootstrap serverBootstrap=new ServerBootstrap();
        serverBootstrap.group(boseGroup,employerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,128)
                .option(ChannelOption.SO_KEEPALIVE,true)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(io.netty.channel.socket.SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline=socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(300,0,0));
                        pipeline.addLast(new HttpServerCodec());
                        pipeline.addLast(new ChunkedWriteHandler());
                        pipeline.addLast(new HttpObjectAggregator(8192));
                        pipeline.addLast(new MyHeaderAnalyzeHandler());
                        pipeline.addLast(new WebSocketServerProtocolHandler("/",true));
                        pipeline.addLast(WEBSOCKET_HANDLER);
                    }

                });
        serverBootstrap.bind(WEBSOCKET_PORT).sync();
    }
}
