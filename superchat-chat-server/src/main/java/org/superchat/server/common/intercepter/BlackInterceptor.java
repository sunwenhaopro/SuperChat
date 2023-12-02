package org.superchat.server.common.intercepter;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.superchat.server.common.exception.Enum.HttpErrorEnum;
import org.superchat.server.common.utils.AssertUtil;
import org.superchat.server.common.utils.ThreadLocalUtil;
import org.superchat.server.common.cache.BlackCache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Set;

@Configuration
@AllArgsConstructor
public class BlackInterceptor implements HandlerInterceptor {
    private BlackCache blackCache;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Set<String> blackSet = blackCache.getBlackSet();
        String uid = ThreadLocalUtil.getUid().toString();
        AssertUtil.isFalse(blackSet.contains(uid), HttpErrorEnum.BLACK_UID_DENIED);
        String ip = request.getHeader("X-Real-IP");
        if (Objects.isNull(ip)) {
            ip = request.getRemoteAddr();
            AssertUtil.isFalse(blackSet.contains(ip), HttpErrorEnum.BLACK_IP_DENIED);
        } else {
            AssertUtil.isFalse(blackSet.contains(ip), HttpErrorEnum.BLACK_IP_DENIED);
        }
        return true;
    }
}
