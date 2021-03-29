package com.example.community.controller;


import com.example.community.entity.*;
import com.example.community.event.EventProducer;
import com.example.community.service.CommentService;
import com.example.community.service.DiscussPostService;
import com.example.community.service.LikeService;
import com.example.community.service.UserSerivice;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;


    @Autowired
    private UserSerivice userSerivice;


    @Autowired
    private CommentService commentService;


    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private RedisTemplate redisTemplate;

    //发布帖子
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJsonString(403, "你还没有登录哦");
        }

        DiscussPost discussPost = new DiscussPost();
        discussPost.setContent(content);
        discussPost.setTitle(title);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);

        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTIEY_TYPE_DISCUSSPOST)
                .setEntityId(discussPost.getId());
        eventProducer.fireEvent(event);


        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, discussPost.getId());

        //报错的情况，后面统一处理
        return CommunityUtil.getJsonString(0, "帖子发布成功");
    }


    //查看帖子详情页
    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("discussPost", discussPost);
        User user = userSerivice.findUserbyId(discussPost.getUserId());
        model.addAttribute("user", user);
        long entityLikeCount = likeService.findEntityLikeCount(ENTIEY_TYPE_DISCUSSPOST, discussPostId);
        model.addAttribute("likeCount", entityLikeCount);

        if (hostHolder.getUser() == null) {
            model.addAttribute("likeStatus", 0);
        } else {
            model.addAttribute("likeStatus", likeService.findEntityLikeStatus(hostHolder.getUser().getId(),
                    ENTIEY_TYPE_DISCUSSPOST, discussPostId));
        }


        page.setLimit(5);
        page.setPath("/detail/" + discussPostId);
        page.setRows(discussPost.getCommentCount());

        List<Comment> comments = commentService.findCommentsByEntity(ENTIEY_TYPE_DISCUSSPOST,
                discussPost.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentsList = new ArrayList<>();

        if (comments != null) {
            for (Comment comment : comments) {

                //将评论和评论者装入map
                Map<String, Object> map1 = new HashMap<>();
                map1.put("comment", comment);
                map1.put("user", userSerivice.findUserbyId(comment.getUserId()));

                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                if (hostHolder.getUser() == null) {
                    map1.put("likeStatus", 0);
                } else {
                    map1.put("likeStatus", likeService.findEntityLikeStatus(hostHolder.getUser().getId(),
                            ENTITY_TYPE_COMMENT, comment.getId()));
                }
                map1.put("likeCount", likeCount);


                List<Comment> replies = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT,
                        comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> relpiesList = new ArrayList<>();

                if (replies != null) {
                    for (Comment reply : replies) {
                        Map<String, Object> map2 = new HashMap<>();

                        long replyLikeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        if (hostHolder.getUser() == null) {
                            map2.put("likeStatus", 0);
                        } else {
                            map2.put("likeStatus", likeService.findEntityLikeStatus(hostHolder.getUser().getId(),
                                    ENTITY_TYPE_COMMENT, reply.getId()));
                        }
                        map2.put("likeCount", replyLikeCount);


                        map2.put("reply", reply);
                        map2.put("replyUser", userSerivice.findUserbyId(reply.getUserId()));
                        User replyTarget = reply.getTargetId() == 0 ? null : userSerivice.findUserbyId(reply.getTargetId());
                        map2.put("replyTarget", replyTarget);
                        relpiesList.add(map2);
                    }
                }
                map1.put("replies", relpiesList);
                map1.put("replyCount", relpiesList.size());
                commentsList.add(map1);
            }
        }
        model.addAttribute("comments", commentsList);

        return "/site/discuss-detail";
    }

    //置顶
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);
        //触发发帖事件
        Event event = new Event()
                .setEntityId(id)
                .setEntityType(ENTIEY_TYPE_DISCUSSPOST)
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId());
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0, "设置置顶成功!");
    }

    //加精
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);
        //触发发帖事件
        Event event = new Event()
                .setEntityId(id)
                .setEntityType(ENTIEY_TYPE_DISCUSSPOST)
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId());
        eventProducer.fireEvent(event);

        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey, id);


        return CommunityUtil.getJsonString(0, "设置精品帖成功!");
    }

    //删帖
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);
        //触发发帖事件
        Event event = new Event()
                .setEntityId(id)
                .setEntityType(ENTIEY_TYPE_DISCUSSPOST)
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId());
        eventProducer.fireEvent(event);

        return CommunityUtil.getJsonString(0, "删除帖子成功!");
    }

    //查询用户发布的帖子
    @GetMapping("/user/posts")
    public String getPosts(@RequestParam("userId") int userId, Model model, Page page, @RequestParam(name = "flag") int flag) {
        int postCount = discussPostService.findDiscussPostRows(userId);
        page.setRows(postCount);
        page.setPath("/user/posts/?userId=" + userId + "&flag=" + flag);

        List<DiscussPost> list = discussPostService.findDiscussPosts(userId, page.getOffset(), page.getLimit(), 1);
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (list != null) {
            for (DiscussPost discussPost : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", discussPost);
                map.put("likeCount", likeService.findEntityLikeCount(CommunityConstant.ENTIEY_TYPE_DISCUSSPOST, discussPost.getId()));
                discussPosts.add(map);
            }
        }

        model.addAttribute("posts", discussPosts);
        model.addAttribute("user", userSerivice.findUserbyId(userId));
        model.addAttribute("flag", flag);
        model.addAttribute("postCount", postCount);
        return "/site/my-post";

    }
}
