package com.example.community.controller;


import com.example.community.entity.Comment;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.Event;
import com.example.community.entity.Page;
import com.example.community.event.EventProducer;
import com.example.community.service.CommentService;
import com.example.community.service.DiscussPostService;
import com.example.community.service.UserSerivice;
import com.example.community.util.CommunityConstant;
import com.example.community.util.HostHolder;
import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class CommentController implements CommunityConstant {


    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserSerivice userSerivice;



    //发布评论
    @PostMapping("/comment/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {

        comment.setUserId(hostHolder.getUser().getId());
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        commentService.addComment(comment);
        if (hostHolder.getUser().getId() != discussPostService.findDiscussPostById(discussPostId).getUserId()) {
            //触发评论事件通知
            Event event = new Event()
                    .setTopic(TOPIC_COMMENT)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityId(comment.getEntityId())
                    .setEntityType(comment.getEntityType())
                    .setData("discussPostId", discussPostId);

            if (comment.getEntityType() == ENTIEY_TYPE_DISCUSSPOST) {
                DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
                event.setEntityUserId(discussPost.getUserId());

                //计算帖子分数
                String postScoreKey = RedisKeyUtil.getPostScoreKey();
                redisTemplate.opsForSet().add(postScoreKey, discussPostId);


            } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
                Comment target = commentService.findCommentById(comment.getId());
                event.setEntityUserId(target.getUserId());
            }
            eventProducer.fireEvent(event);
        }


        if (comment.getEntityType() == ENTIEY_TYPE_DISCUSSPOST) {
            //触发发帖事件
            Event event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTIEY_TYPE_DISCUSSPOST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);
        }

        return "redirect:/detail/" + discussPostId;
    }


    //查询某人发表的评论
    @GetMapping("/user/comments")
    public String getComments(@RequestParam(name = "userId") int userId, @RequestParam(name = "flag") int flag,
                              Model model, Page page) {

        int count = commentService.findCountComment(userId, ENTIEY_TYPE_DISCUSSPOST);
        page.setPath("/user/comments?userId=" + userId + "&flag=" + flag);
        page.setRows(count);

        List<Comment> comments = commentService.findCommentsByUserId(userId, ENTIEY_TYPE_DISCUSSPOST,
                page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();

        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);
                map.put("post", discussPostService.findDiscussPostById(comment.getEntityId()));
                list.add(map);
            }
        }
        model.addAttribute("commentCount", count);
        model.addAttribute("comments", list);
        model.addAttribute("flag", flag);
        model.addAttribute("user", userSerivice.findUserbyId(userId));
        return "/site/my-reply";
    }
}
