package com.example.malllite.config;

import com.example.malllite.interceptor.LoginInterceptor;
import com.example.malllite.interceptor.PermissionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    private final PermissionInterceptor permissionInterceptor;

    public WebMvcConfig(LoginInterceptor loginInterceptor,
                        PermissionInterceptor permissionInterceptor) {
        this.loginInterceptor = loginInterceptor;
        this.permissionInterceptor = permissionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",

                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                )
                .order(1);

        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",

                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                )
                .order(2);
    }
}