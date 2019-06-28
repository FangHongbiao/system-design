package com.fhb.sso.web.interceptor;

import com.alibaba.druid.util.StringUtils;
import com.fhb.sso.web.domain.User;
import com.fhb.sso.web.redis.RedisUtil;
import com.fhb.sso.web.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/28
 * Time: 21:22
 *
 * @author hbfang
 */

public class WebInterceptor implements HandlerInterceptor {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = CookieUtils.getCookie(request, "token");

        System.out.println("---");
        if (StringUtils.isEmpty(token)) {
            response.sendRedirect("http://localhost:8080/login?url=http://localhost:8081");
            return false;
        }
        return true;
    }


    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // 已登录状态
        if (user != null) {
            if (modelAndView != null) {
                modelAndView.addObject("user", user);
            }
        }

        // 未登录状态
        else {
            String token = CookieUtils.getCookie(request, "token");

            if (!StringUtils.isEmpty(token)) {

                String username = (String) redisUtil.get(token);

                if (username != null) {
                    user = (User) redisUtil.get(username);

                    // 创建局部会话
                    request.getSession().setAttribute("user", user);
                    if (modelAndView != null) {
                        modelAndView.addObject("user", user);
                    }
                }

            }
        }

        // 二次确认
        if (user == null) {
            response.sendRedirect("http://localhost:8080/login?url=http://localhost:8081");
        }
    }
}
