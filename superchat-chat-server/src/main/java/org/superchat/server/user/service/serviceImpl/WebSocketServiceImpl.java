package org.superchat.server.user.service.serviceImpl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.superchat.server.common.event.UserOnlineEvent;
import org.superchat.server.common.event.UserOfflineEvent;
import org.superchat.server.common.utils.NettyUtil;
import org.superchat.server.user.dao.UserDao;
import org.superchat.server.user.domain.entity.IpInfo;
import org.superchat.server.user.domain.entity.User;
import org.superchat.server.user.domain.enums.ChatActiveStatusEnum;
import org.superchat.server.user.domain.enums.RoleEnum;
import org.superchat.server.user.domain.enums.WSBaseResp;
import org.superchat.server.user.domain.vo.response.ws.WSBlack;
import org.superchat.server.user.domain.vo.response.ws.WSLoginSuccess;
import org.superchat.server.user.domain.vo.response.ws.WSLoginUrl;
import org.superchat.server.user.dto.WSChannelExtraDTO;
import org.superchat.server.user.service.LoginService;
import org.superchat.server.user.service.RoleService;
import org.superchat.server.user.service.WebSocketService;
import org.superchat.server.user.service.convert.WSConvert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.superchat.server.common.config.ThreadPoolConfig.WS_EXECUTOR;

@Service
@RequiredArgsConstructor //只注入非final修饰
public class WebSocketServiceImpl implements WebSocketService {
    public static final int DURATION = 30;
    public static final int MAXIMUM_SIZE = 1000;
    private static final ConcurrentHashMap<Channel, WSChannelExtraDTO> ONLINE_WS_MAP = new ConcurrentHashMap<>();
    //多端登录
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<Channel>> ONLINE_UID_MAP = new ConcurrentHashMap<>();
    private static final Cache<Integer, Channel> TEM_LOGIN_CODE_MAP = Caffeine.newBuilder()
            .maximumSize(MAXIMUM_SIZE)
            .expireAfterWrite(DURATION, TimeUnit.MINUTES)
            .build();
    private final LoginService loginService;
    private final WxMpService wxMpService;
    private final UserDao userDao;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RoleService roleService;


    @Resource
    @Qualifier(value = WS_EXECUTOR)
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    //维护连接的WS
    @Override
    public void connect(Channel channel) {
        ONLINE_WS_MAP.put(channel, new WSChannelExtraDTO());
    }

    //处理Login事件
    @SneakyThrows
    @Override
    public void handlerLoginReq(Channel channel) {
        //生成code
        Integer code = generateRandomCode(channel);
        //请求WX二维码
        WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(code, DURATION);
        //发送结果
        WSBaseResp<WSLoginUrl> resp = WSConvert.toWSBaseResp(wxMpQrCodeTicket.getUrl());
        sendMsg(channel, resp);
    }

    @Override
    public void remove(Channel channel) {
        WSChannelExtraDTO wsChannelExtraDTO = ONLINE_WS_MAP.get(channel);
        Optional<Long> uid = Optional.ofNullable(wsChannelExtraDTO).map(WSChannelExtraDTO::getUid);
        boolean offlineAll = offline(channel, uid);
        if (uid.isPresent() && offlineAll) {
            User user = new User();
            user.setId(uid.get());
            user.setLastOptTime(new Date());
            applicationEventPublisher.publishEvent(new UserOfflineEvent(this, user));
        }
        ONLINE_WS_MAP.remove(channel);
    }

    private boolean offline(Channel channel, Optional<Long> uid) {
        ONLINE_WS_MAP.remove(channel);
        if (uid.isPresent()) {
            CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uid.get());
            if (CollectionUtil.isNotEmpty(channels)) {
                //只下线该终端
                channels.removeIf(ch -> Objects.equals(ch, channel));
                return true;
            }
        }
        return true;
    }

    //处理扫码
    @Override
    public void scanLoginSuccess(Long uid, Integer code) {
        Channel channel = TEM_LOGIN_CODE_MAP.getIfPresent(code);
        if (Objects.isNull(channel)) {
            return;
        }
        User user = userDao.getById(uid);
        String token = loginService.login(uid);
        this.loginSuccess(channel, user, token);
        TEM_LOGIN_CODE_MAP.invalidate(code);
    }

    //携带token的认证
    @Override
    public void authorize(Channel channel, String token) {
        Long uid = loginService.getUidIfPresent(token);
        if (Objects.isNull(uid)) {
            WSBaseResp<WSLoginSuccess> resp = WSConvert.toInvalidToken();
            sendMsg(channel, resp);
            return;
        }
        User user = userDao.getById(uid);
        this.loginSuccess(channel, user, token);
    }

    @Override
    public void broadcastMsg(WSBlack black) {

    }

    @Override
    public void sendToUser(Long uid, WSBaseResp<?> wsBaseMsg) {
        CopyOnWriteArrayList<Channel> channels = ONLINE_UID_MAP.get(uid);
        if (CollectionUtil.isNotEmpty(channels)) {
            channels.forEach(channel ->
                    threadPoolTaskExecutor.execute(() -> {
                        this.sendMsg(channel, wsBaseMsg);
                    }));
        }
    }

    @Override
    public void sendToAllOnline(WSBaseResp<?> wsBaseMsg, Long skipUid) {
        ONLINE_WS_MAP.forEach((channel, ext) -> {
            if (Objects.nonNull(skipUid) && Objects.equals(ext.getUid(), skipUid)) {
                return;
            }
            threadPoolTaskExecutor.execute(() -> sendMsg(channel, wsBaseMsg));
        });
    }

    //生成随机CODE
    private Integer generateRandomCode(Channel channel) {
        Integer code;
        do {
            code = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
        } while (Objects.nonNull(TEM_LOGIN_CODE_MAP.asMap().putIfAbsent(code, channel)));
        return code;
    }

    //token或者扫码登陆成功
    private void loginSuccess(Channel channel, User user, String token) {
        boolean hasPower = roleService.hasPower(user.getId(), RoleEnum.ADMIN);
        WSBaseResp<WSLoginSuccess> loginSuccessResp = WSConvert.toLoginSuccessResp(user, token, hasPower);
        //更新OnlineMap
        this.updateOnline(channel, user);
        //发送登录成功信息
        sendMsg(channel, loginSuccessResp);
        //事件
        user.setLastOptTime(new Date());
        IpInfo ipInfo = new IpInfo();
        ipInfo.setUpdateIp(NettyUtil.getAttr(channel, NettyUtil.IP));
        user.setIpInfo(ipInfo);
        user.setActiveStatus(ChatActiveStatusEnum.ONLINE.getStatus());
        applicationEventPublisher.publishEvent(new UserOnlineEvent(this, user));
    }

    private WSChannelExtraDTO getOrInitChannelExt(Channel channel) {
        //并发安全
        WSChannelExtraDTO dto = ONLINE_WS_MAP.getOrDefault(channel, new WSChannelExtraDTO());
        WSChannelExtraDTO old = ONLINE_WS_MAP.putIfAbsent(channel, dto);
        return Objects.isNull(old) ? dto : old;
    }

    private void updateOnline(Channel channel, User user) {
        this.getOrInitChannelExt(channel).setUid(user.getId());
        CopyOnWriteArrayList<Channel> tem=new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Channel> channels= ONLINE_UID_MAP.getOrDefault(user.getId(),tem);
        if(channels.size()==0)
        {
            ONLINE_UID_MAP.put(user.getId(), channels);
        }
        channels.add(channel);
        NettyUtil.setAttr(channel, NettyUtil.UID, user.getId());
    }

    private void sendMsg(Channel channel, WSBaseResp<?> resp) {
        channel.writeAndFlush(new TextWebSocketFrame(JSONUtil.toJsonStr(resp)));
    }

}
