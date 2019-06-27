package com.fhb.sso.controller;

import com.alibaba.druid.util.StringUtils;
import com.fhb.sso.domain.User;
import com.fhb.sso.redis.RedisUtil;
import com.fhb.sso.service.LoginService;
import com.fhb.sso.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/6/27
 * Time: 11:20
 *
 * @author hbfang
 */

@Controller
public class LoginController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private LoginService loginService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(@RequestParam(required = false) String url,
                        HttpServletRequest request, Model model) {
        String token = CookieUtils.getCookie(request, "token");

        if (!StringUtils.isEmpty(token)) {
            Object usernameObj = redisUtil.get(token);

            if (usernameObj != null) {
                String username = (String) usernameObj;
                Object userObj = redisUtil.get(username);

                if (userObj != null) {
                    try {
                        User user = (User) userObj;

                        if (!StringUtils.isEmpty(url)) {
                            return "redirect:" + url;
                        }
                        model.addAttribute("user", user);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return "login";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestParam String username, @RequestParam String password, @RequestParam(required = false) String url
            , HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {

        User user = loginService.login(username, password);

        if (user != null) {
            String token = UUID.randomUUID().toString();
            boolean isSuccess = redisUtil.set(token, username, 60 * 60 * 24);
            if (isSuccess) {
                CookieUtils.setCookie(response, "token", token);
                if (StringUtils.isEmpty(url)) {
                    url = "/login";
                }
                return "redirect:" + url;
            } else {
                // redis设置失败了
                redirectAttributes.addFlashAttribute("message", "服务器异常");
                return "redirect:/login";
            }
        } else {
            System.out.println("------------");
            redirectAttributes.addFlashAttribute("message", "用户名或密码错误");
            return "redirect:/login";
        }
    }
}
