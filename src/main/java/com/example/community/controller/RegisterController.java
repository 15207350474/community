package com.example.community.controller;


import com.example.community.entity.User;
import com.example.community.service.UserSerivice;
import com.example.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@Controller
public class RegisterController implements CommunityConstant {

    @Autowired
    private UserSerivice userSerivice;

    @GetMapping("/register")
    public String getRegisterPage() {
        return "/site/register";
    }




    //注册帐号
    @PostMapping("/register")
    public String register(Model model, User user) {
        Map<String, Object> map = userSerivice.register(user);

        if (map.isEmpty()) { //如果map是空的，说明注册成功并且向注册邮箱发送了激活邮件
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封邮件，请尽快激活!");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        }
        //注册失败把异常都装进model里返回注册页面
        model.addAttribute("usernameMsg", map.get("usernameMsg"));
        model.addAttribute("pssswordMsg", map.get("passwordMsg"));
        model.addAttribute("emailMsg", map.get("emailMsg"));
        return "/site/register";
    }


    //激活帐号
    @GetMapping("/activation/{userId}/{activationCode}")
    public String activation(Model model, @PathVariable("userId") int userId,
                             @PathVariable("activationCode") String activationCode) {
        int res = userSerivice.activation(userId, activationCode);
        if (res == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用了");
            model.addAttribute("target", "/login");
        } else if (res == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "激活无效，您的账号已经激活过了");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，激活码不正确");
            model.addAttribute("target", "/index");
        }

        return "/site/operate-result";
    }
}
