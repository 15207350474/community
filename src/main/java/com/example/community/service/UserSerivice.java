package com.example.community.service;


import com.example.community.entity.LoginTicket;
import com.example.community.entity.User;
import com.example.community.mapper.LoginTicketMapper;
import com.example.community.mapper.UserMapper;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.MailClient;
import com.example.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserSerivice implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPth;


    //这个方法调用非常频繁，每次请求都要调用
    public User findUserbyId(int id) {
        User user = getUserFromCache(id);  //先从缓存中取
        if (user == null) {
            user = addCache(id); //如果缓存没有，从数据库中取，在加入缓存
        }
        return user;
    }

    //注册帐号
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //空值检验
        if (user == null) {
            throw new IllegalArgumentException("用户不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "帐号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        if (userMapper.selectByName(user.getUsername()) != null) {
            map.put("usernameMsg", "该账号已存在");
            return map;
        }
        if (userMapper.selectByEmail(user.getEmail()) != null) {
            map.put("emailMsg", "该邮箱已被注册");
            return map;
        }

        //新增用户
        user.setSalt(CommunityUtil.getRandomString().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0); //普通用户
        user.setStatus(0); //未激活
        user.setActivationCode(CommunityUtil.getRandomString());
        user.setHeaderUrl(String.format("http://images/nowcoder.com/head/%dt.png", (int)(Math.random() * 0) + 1000));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //给新注册的用户发送激活邮件
        Context context = new Context();
        context.setVariable("username", user.getUsername());
        String url = domain + contextPth + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String process = templateEngine.process("/mail/activation", context); //加载邮件模板
        mailClient.sendMail(user.getEmail(), "激活账号", process);
        return map;
    }


    //激活帐号
    public int activation(int userId, String activationCode) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) return ACTIVATION_REPEAT; //如果用户状态为1，说明帐号已经激活
        if (user.getActivationCode().equals(activationCode)) { //判断激活码是否相等
            userMapper.updateStatus(userId, 1); //修改用户状态
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }
        return ACTIVATION_FAILURE;
    }


    //登录
    public Map<String, String> login(String username, String password, int expiredSeconds) {
        Map<String, String> map = new HashMap<>();
        //空值检测
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        //验证帐号是否存在
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "用户名不存在");
            return map;
        }
        //验证帐号状态是否正常
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "改帐号未激活");
            return map;
        }
        //验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码错误");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.getRandomString());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        //loginTicketMapper.insertLoginTicket(loginTicket);
        //存入redis
        String loginTicketKey = RedisKeyUtil.getLoginTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(loginTicketKey, loginTicket);

        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    //退出
    public void exit(String ticket) {

        String loginTicketKey = RedisKeyUtil.getLoginTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(loginTicketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(loginTicketKey, loginTicket);
    }

    //找回密码
    public void sendFindBackPasswordMail(User user, String code) {

        //设置邮件参数
        Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("code", code);
        //将Html封装成邮件内容
        String process = templateEngine.process("mail/forget", context);
        //发送邮件
        mailClient.sendMail(user.getEmail(), "找回密码", process);

    }

    //重置密码
    public void resetPassword(String username, String password) {
        User user = userMapper.selectByName(username);
        userMapper.updatePassword(user.getId(), CommunityUtil.md5(password + user.getSalt()));
    }


    //查询登录凭证
    public LoginTicket findLoginTicketByTicket(String ticket) {
        String loginTicketKey = RedisKeyUtil.getLoginTicketKey(ticket);
        return (LoginTicket)redisTemplate.opsForValue().get(loginTicketKey);
    }


    //修改头像
    public int updateHeader(int userId, String headerUrl) {
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    //修改密码
    public Map<String, Object> updatePassowrd(int userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        Map<String, Object> map = new HashMap<>();
        if (!oldPassword.equals(user.getPassword())) {
            map.put("passwordError", "密码修改失败，旧密码错误");
        } else {
            newPassword = CommunityUtil.md5(newPassword + user.getSalt());
            userMapper.updatePassword(userId, newPassword);
            clearCache(userId);
            map.put("result", "密码修改成功");
        }

        return map;

    }

    public User findByUserName(String username) {
        return userMapper.selectByName(username);
    }


    //从缓存中取用户信息
    private User getUserFromCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User)redisTemplate.opsForValue().get(userKey);
    }

    //将用户信息存入缓存（过期时间1个小时）
    private User addCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS); //缓存1个小时
        return user;
    }

    //清除缓存中的用户信息 （当用户信息发生变化，清除缓存）
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }


    //获得用户的权限
    public Collection<? extends GrantedAuthority> getAuthority(int userId) {
        User user = userMapper.selectById(userId);
        List<GrantedAuthority> list = new ArrayList<>();

        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:return AUTHORITY_ADMIN;
                    case 2:return AUTHORITY_MODERATOR;
                    default:return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
}
