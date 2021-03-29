package com.example.community.controller;

import com.example.community.entity.DiscussPost;
import com.example.community.entity.Page;
import com.example.community.service.DiscussPostService;
import com.example.community.service.LikeService;
import com.example.community.service.UserSerivice;
import com.example.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于访问首页的Controller
 */

@Controller
public class HomeController {

    @Autowired
    private UserSerivice userSerivice;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;


    //访问主页
    @GetMapping("/index")
    public String index(Model model, Page page, @RequestParam(name = "orderMode", defaultValue = "0") int orderMode) {
        page.setPath("/index/?orderMode=" + orderMode);
        page.setRows(discussPostService.findDiscussPostRows(0));
        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffset(), page.getLimit(), orderMode);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost discussPost : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                map.put("user", userSerivice.findUserbyId(discussPost.getUserId()));
                map.put("likeCount", likeService.findEntityLikeCount(CommunityConstant.ENTIEY_TYPE_DISCUSSPOST, discussPost.getId()));
                discussPosts.add(map);
            }
        }

        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("orderMode", orderMode);
        return "index";
    }


    //去往服务器内部错误页面
    @GetMapping("/error")
    public String getErrorPage() {
        return "/error/500";
    }

    //去往没有权限访问页面
    @GetMapping("/denied")
    public String getDeniedPage() {
        return "/error/404";
    }


}
