package com.example.community.controller;


import com.example.community.entity.Event;
import com.example.community.entity.User;
import com.example.community.event.EventProducer;
import com.example.community.service.LikeService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞功能（异步请求）
 */

@Controller
public class LikeController implements CommunityConstant {


    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;


    @Autowired
    private RedisTemplate redisTemplate;

    //点赞功能(给某个实体点赞)
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int discussPostId) {
        User user = hostHolder.getUser();
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        int entityLikeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", entityLikeStatus);

        //触发点赞事件
        if (entityLikeStatus == 1 && user.getId() != entityUserId) {
            Event event = new Event()
                    .setUserId(user.getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setTopic(TOPIC_LIKE)
                    .setEntityUserId(entityUserId)
                    .setData("discussPostId", discussPostId);
            eventProducer.fireEvent(event);
        }

        //计算帖子分数
        if (entityType == ENTIEY_TYPE_DISCUSSPOST) {
            String postScoreKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey, discussPostId);
        }

        return CommunityUtil.getJsonString(0, null, map);
    }

}
