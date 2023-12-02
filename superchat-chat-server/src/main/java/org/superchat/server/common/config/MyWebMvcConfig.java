package org.superchat.server.common.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.superchat.server.common.intercepter.BlackInterceptor;
import org.superchat.server.common.intercepter.TokenInterceptor;
@AllArgsConstructor
@Configuration
public class MyWebMvcConfig implements WebMvcConfigurer {
    private final TokenInterceptor tokenInterceptor;
    private final BlackInterceptor blackInterceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor).order(0)
                .addPathPatterns("/**")
                .excludePathPatterns("/wx/portal/public/**");
        registry.addInterceptor(blackInterceptor).order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/wx/portal/public/**");

    }
}
