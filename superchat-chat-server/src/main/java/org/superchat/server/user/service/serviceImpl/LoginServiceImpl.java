package org.superchat.server.user.service.serviceImpl;

import lombok.AllArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.superchat.server.common.config.ThreadPoolConfig;
import org.superchat.server.common.constant.RedisKey;
import org.superchat.server.common.utils.JwtUtil;
import org.superchat.server.user.service.LoginService;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
public class LoginServiceImpl implements LoginService {
    private final RedisTemplate<String,Object> redisTemplate;
    private final JwtUtil jwtUtil;


    @Override
    @Async(ThreadPoolConfig.BEAN_NAME)
    public void refreshTokenExpire(String token) {
       redisTemplate.opsForValue().getAndExpire(RedisKey.getKey(RedisKey.USER_TOKEN_STRING,token),RedisKey.TOKEN_EXPIRE, TimeUnit.DAYS);
    }

    @Override
    public Long getUidIfPresent(String token) {
        Object uid= redisTemplate.opsForValue().get(RedisKey.getKey(RedisKey.USER_TOKEN_STRING,token));
        if(Objects.nonNull(uid)){
            return Long.parseLong(uid.toString());
        }
        return null;
    }

    @Override
    public String login(Long uid) {
        String token=jwtUtil.creatToken(uid);
        //存入redis
        redisTemplate.opsForValue().set(RedisKey.getKey(RedisKey.USER_TOKEN_STRING,token),uid,
                                        RedisKey.TOKEN_EXPIRE, TimeUnit.DAYS);
        return token;
    }
}
