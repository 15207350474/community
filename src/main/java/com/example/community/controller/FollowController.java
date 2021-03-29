package com.example.community.controller;


import com.example.community.entity.Event;
import com.example.community.entity.Page;
import com.example.community.entity.User;
import com.example.community.event.EventProducer;
import com.example.community.service.FollowService;
import com.example.community.service.UserSerivice;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {


    @Autowired
    private FollowService followService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private UserSerivice userSerivice;

    @Autowired
    private HostHolder hostHolder;



    //关注某个实体
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.follw(user.getId(), entityType, entityId);


        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId)
                .setUserId(user.getId());
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0, "已关注");

    }

    //取消关注某个实体
    @PostMapping("/unfollow")
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJsonString(0, "已取消关注");

    }

    //查询某个用户的粉丝
    @GetMapping("/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userSerivice.findUserbyId(userId);
        User curUser = hostHolder.getUser();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        page.setLimit(10);
        page.setPath("/followers/" + userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_User, userId));

        model.addAttribute("user", user);

        List<User> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (followers != null) {
            for (User follower : followers) {
                Map<String, Object> map = new HashMap<>();
                if (curUser == null) {
                    map.put("hasFollowed", false);
                } else {
                    map.put("hasFollowed", followService.findFollowStatus(curUser.getId(), ENTITY_TYPE_User, follower.getId()));
                }
                map.put("follower", follower);
                list.add(map);
            }
        }
        model.addAttribute("followers", list);

        return "/site/follower";

    }

    //查询某个用户的关注
    @GetMapping("/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userSerivice.findUserbyId(userId);
        User curUser = hostHolder.getUser();
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        page.setLimit(10);
        page.setPath("/followees/" + userId);
        page.setRows((int)followService.findFolloweeCount(ENTITY_TYPE_User, userId));

        model.addAttribute("user", user);

        List<User> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (followees != null) {
            for (User followee : followees) {
                Map<String, Object> map = new HashMap<>();
                if (curUser == null) {
                    map.put("hasFollowed", false);
                } else {
                    map.put("hasFollowed", followService.findFollowStatus(curUser.getId(), ENTITY_TYPE_User, followee.getId()));
                }
                map.put("followee", followee);
                list.add(map);
            }
        }
        model.addAttribute("followees", list);

        return "/site/followee";
    }
}
