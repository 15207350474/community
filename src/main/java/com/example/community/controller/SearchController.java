package com.example.community.controller;


import com.example.community.entity.DiscussPost;
import com.example.community.entity.Page;
import com.example.community.service.ElasticSearchService;
import com.example.community.service.LikeService;
import com.example.community.service.UserSerivice;
import com.example.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private UserSerivice userSerivice;

    @Autowired
    private LikeService likeService;


    @GetMapping("/search")
    public String search(String keyword, Page page, Model model) {
        org.springframework.data.domain.Page<DiscussPost> discussPosts =
                elasticSearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        List<Map<String, Object>> list = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost discussPost : discussPosts) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("discussPost", discussPost);
                map.put("user", userSerivice.findUserbyId(discussPost.getUserId()));
                map.put("likeCount", likeService.findEntityLikeCount(ENTIEY_TYPE_DISCUSSPOST, discussPost.getId()));
                list.add(map);
            }
        }
        model.addAttribute("discussPosts", list);
        model.addAttribute("keyword", keyword);
        page.setPath("/search?keyword=" + keyword);
        page.setRows(discussPosts == null ? 0 : (int)discussPosts.getTotalElements());
        return "/site/search";
     }

}
