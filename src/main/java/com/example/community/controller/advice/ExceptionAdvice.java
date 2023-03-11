package com.example.community.controller.advice;

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

@ControllerAdvice(annotations = Controller.class) //只扫描带有Controller注解的bean，统一处理这些bean的异常
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);
    @ExceptionHandler({Exception.class}) //{}内为可以被此方法处理的异常类型
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 记录错误日志
        logger.error("服务器发生异常："+e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }
        //判断发生异常的请求是普通请求还是AJAX请求
        final String xRequestedWith = request.getHeader("x-requested-with");
        if ("XMLHttpRequest".equals(xRequestedWith)) { //异步请求
            response.setContentType("application/plain;charset=utf-8");
            final PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1, "服务器异常！"));
        } else { //普通请求，重新定向到500页面
            response.sendRedirect(request.getContextPath()+ "/error"); //http:8080/community/error
        }
    }

}
