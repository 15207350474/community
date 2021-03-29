package com.example.community.config;


import com.example.community.service.UserSerivice;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Autowired
    private UserSerivice userSerivice;


    @Override
    public void configure(WebSecurity web) throws Exception {
        //忽略静态资源的访问
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权(设置需要权限才能访问的所有请求)
        //认证请求
        http.authorizeRequests().antMatchers(
                "/comment/add/{discussPostId}",
                "/add",
                "/follow",
                "/unfollow",
                "/like",
                "/exit",
                "/notice/**",
                "/letter/**",
                "/upload",
                "/updatePassword"
        ).hasAnyAuthority(
                AUTHORITY_ADMIN,
                AUTHORITY_USER,
                AUTHORITY_MODERATOR
        ).antMatchers(
                "/top",
                "/wonderful"
        ).hasAnyAuthority(
                AUTHORITY_MODERATOR
        ).antMatchers(
                "/delete",
                "/data/**"
        ).hasAnyAuthority(
                AUTHORITY_ADMIN
        ).anyRequest().permitAll() //除了以上路径，其他路径不需要权限都可以访问
        .and().csrf().disable(); //取消csrf检查


        //访问了没有权限访问的请求的处理方案
        http.exceptionHandling().authenticationEntryPoint(
                new AuthenticationEntryPoint() {
                    //没有登录时做的处理
                    @Override
                    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {

                        //判断请求方式是异步还是普通请求
                        String header = httpServletRequest.getHeader("x-requested-with");
                        if ("XMLHttpRequest".equals(header)) { //如果是异步请求, 则返回JSON字符串
                            httpServletResponse.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = httpServletResponse.getWriter();
                            writer.write(CommunityUtil.getJsonString(403, "你还没有登录！"));
                        } else { //如果是普通请求，重定向到登录页面
                            httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
                        }


                    }
                }
        ).accessDeniedHandler(new AccessDeniedHandler() {
            //权限不足时的处理
            @Override
            public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                //判断请求方式的异步请求还是普通请求
                String header = httpServletRequest.getHeader("x-requested-with");
                if ("XMLHttpRequest".equals(header)) { //如果是异步请求
                    httpServletResponse.setContentType("application/plain;charset=utf-8");
                    PrintWriter writer = httpServletResponse.getWriter();
                    writer.write(CommunityUtil.getJsonString(403, "没有权限！"));
                } else { //如果是普通请求,重定向到权限不足的页面
                    httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
                }
            }
        });

        //Security底层默认会拦截/logout请求，进行退出处理
        //覆盖它的逻辑才能使用自己写的逻辑
//        http.logout().logoutUrl("/sadsahdkj");
    }
}
