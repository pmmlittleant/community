package com.example.community.controller.interceptor;

import com.example.community.entity.LoginTicket;
import com.example.community.entity.User;
import com.example.community.service.UserService;
import com.example.community.util.CookieUtil;
import com.example.community.util.HostHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从Cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            //查询凭证
            final LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {

                // 根据凭证查询用户
                final User user = userService.findUserById(loginTicket.getUserId());

                //在本次请求中持有用户
                // 每个服务器可以响应多个浏览器的请求，服务器对每一个请求的处理会创立一个独立的线程处理访问的请求因此是多线程的环境
                // 在多线程并发的情况下访问一个变量不会产生冲突需要考虑线程的隔离（每个线程单独存一份，线程之间不会相互干扰）
                // 在多线程中隔离存放对象：ThreadLocal(set方法：获得当前线程，以当前线程为key获得当前线程的Map对象，将变量存入当前线程的map中）

                hostHolder.setUser(user); //通过一个ThreadLocal的封装类将user存入当前请求的线程中，请求结束前都能访问到该user

                // 构建用户认证的结果，并存入SecurityContext,以便于Security进行授权。
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        final User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear(); //请求结束，将关闭线程，需要清除线程中存放的变量（user)，避免占用内存
    }
}
