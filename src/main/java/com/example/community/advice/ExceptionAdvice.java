package com.example.community.advice;


import com.example.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * 统一处理Controller异常, 该类会捕获所有controller抛出的异常
 */

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler(Exception.class) //捕获所有异常
    public void handleException(Exception e, HttpServletResponse response, HttpServletRequest request) throws IOException {
        //记录日志并且输出异常
        logger.error("服务器发生异常：" + e.getMessage());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            logger.error(stackTraceElement.toString());
        }

        //判断请求是异步还是普通请求
        String header = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(header)) { //如果请求是异步请求
            response.setContentType("application/plalin;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJsonString(1, "服务器异常!"));
        } else { //如果是普通请求，跳回500错误页面
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
