package com.example.community.controller;


import com.example.community.entity.User;
import com.example.community.service.UserSerivice;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserSerivice userSerivice;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @GetMapping("/login")
    public String getLoginPage() {
        return "site/login";
    }

    //向浏览器输出验证码
    /**
     *把验证码存入Redis中，给登录页面返回一个Cookie的key做为验证码的key 设置60秒自动失效
     */
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage bufferedImage = kaptchaProducer.createImage(text);

        //设置验证码的所属用户标识
        String kaptchaOwner = CommunityUtil.getRandomString();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //将验证码存入redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS); //超过60秒失效

        //将验证码输出给浏览器
        response.setContentType("image/png");
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            ImageIO.write(bufferedImage, "png", outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //用户登录
    @PostMapping("/login")
    public String login(String username, String password, String code, boolean isRememberMe, Model model,
                        HttpServletResponse response, @CookieValue("kaptchaOwner") String kaptchaOwner) {

        //检查验证码
        //String kaptcha = (String)session.getAttribute("kaptcha");
        String kaptcha = null;
        if (!StringUtils.isBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String)redisTemplate.opsForValue().get(kaptchaKey);
        }


        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确");
            return "/site/login";
        }

        //检查帐号密码
        int expiredSeconds = isRememberMe ? CommunityConstant.REMEMBER_EXPIRED_SECONDS : CommunityConstant.DEFAULT_EXPIRED_SECONDS;
        Map<String, String> map = userSerivice.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket"));
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {

            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    //用户退出
    @GetMapping("/exit")
    public String exit(@CookieValue("ticket") String ticket) {
        userSerivice.exit(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    //忘记密码
    @GetMapping("/forget")
    public String forget() {
        return "site/forget";
    }

    //发送找回密码验证码
    @PostMapping("/sendCode")
    @ResponseBody
    public String sendCode(String username, Model model) {
        //参数校验
        if (StringUtils.isBlank(username)) {
            return CommunityUtil.getJsonString(0, "用户名不能为空！");
        }
        User user = userSerivice.findByUserName(username);
        if (user == null) {
            return CommunityUtil.getJsonString(0, "用户名不存在！");
        }
        //设置验证码，并存入redis
        String code = kaptchaProducer.createText();
        String findBackPasswordCodeKey = RedisKeyUtil.getFindBackPasswordCodeKey();
        redisTemplate.opsForValue().set(findBackPasswordCodeKey, code, 60 * 5, TimeUnit.SECONDS);
        //发送验证码邮件
        userSerivice.sendFindBackPasswordMail(user, code);
        return CommunityUtil.getJsonString(1, "验证码已发送到你的邮箱，5分钟内有效，超时请重新获取");
    }

    //重置密码
    @PostMapping("/reset")
    public String resetPassword(String username, String password, String code, Model model) {
        System.out.println(code);
        if (StringUtils.isBlank(code)) {
            model.addAttribute("codeMsg", "验证码不能为空！");
            model.addAttribute("username", username);
            model.addAttribute("password", password);
            return "site/forget";
        }
        String findBackPasswordCodeKey = RedisKeyUtil.getFindBackPasswordCodeKey();
        String redisCode = (String)redisTemplate.opsForValue().get(findBackPasswordCodeKey);
        if (redisCode == null) {
            model.addAttribute("codeMsg", "验证码已过期，请重新获取！");
            model.addAttribute("username", username);
            model.addAttribute("password", password);
            return "site/forget";
        }
        if (!code.equals(redisCode)) {
            model.addAttribute("codeMsg", "验证码错误！");
            model.addAttribute("username", username);
            model.addAttribute("password", password);
            return "site/forget";
        }


        userSerivice.resetPassword(username, password);
        model.addAttribute("target", "/login");
        model.addAttribute("msg", "您的账号已经成功重置密码！");
        return "site/operate-result";
    }
}
