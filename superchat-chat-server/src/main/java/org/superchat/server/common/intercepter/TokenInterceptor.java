package org.superchat.server.common.intercepter;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.superchat.server.common.utils.ThreadLocalUtil;
import org.superchat.server.user.service.LoginService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Configuration
@AllArgsConstructor
public class TokenInterceptor implements HandlerInterceptor {
    public static final String AUTHRIZATION_HEADER = "Authorization";
    private  final LoginService loginService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader(AUTHRIZATION_HEADER);
        if(Objects.nonNull(token))
        {
            Long uid=loginService.getUidIfPresent(token);
            if(Objects.nonNull(uid))
            {
                Map<String,Object> map=new HashMap<>(2);
                map.put(ThreadLocalUtil.UID,uid);
                ThreadLocalUtil.setMap(map);
                return true;
            }
            return false;
        }else{
            return request.getRequestURI().contains("public");
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
