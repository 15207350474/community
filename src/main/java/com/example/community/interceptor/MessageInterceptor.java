package com.example.community.interceptor;


import com.example.community.entity.User;
import com.example.community.service.MessageService;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {


    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;


    //该方法运行在controller后，return回html页面之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int letterCount = messageService.findLetterUnreadCount(user.getId(), null);
            int noticeCount = messageService.findNoticeUnreadCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount", letterCount + noticeCount);
        }

    }
}
