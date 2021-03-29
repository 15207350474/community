package com.example.community.config;


import com.example.community.interceptor.DataInterceptor;
import com.example.community.interceptor.LoginInterceptor;
import com.example.community.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 配置拦截器
 */

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private MessageInterceptor messageInterceptor;


    @Autowired
    private DataInterceptor dataInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).excludePathPatterns("/static/**");
        registry.addInterceptor(messageInterceptor).excludePathPatterns("/static/**");
        registry.addInterceptor(dataInterceptor).excludePathPatterns("/static/**");
    }



}
