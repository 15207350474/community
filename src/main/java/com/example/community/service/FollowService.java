package com.example.community.service;


import com.example.community.entity.User;
import com.example.community.util.CommunityConstant;
import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 使用Redis实现关注功能（关注列表按关注时间排序，使用zset类型, 使用事务）
 */

@Service
public class FollowService implements CommunityConstant {



    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserSerivice userSerivice;


    //关注某个实体
    public void follw(int userId, int entityType, int entityId) {

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
                redisOperations.multi();

                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());

                return redisOperations.exec();
            }
        });
    }


    //对某个实体取消关注
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);

                redisOperations.multi();

                redisOperations.opsForZSet().remove(followeeKey, entityId);
                redisOperations.opsForZSet().remove(followerKey, userId);

                return redisOperations.exec();
            }
        });
    }

    //查询某人对某实体的关注状态
    public boolean findFollowStatus(int userId, int enitiyType, int entityId) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, enitiyType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null; //如果没有分数，说明没有关注
    }


    //查询某人关注的实体数量
    public long findFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey); //zCard和size差不多
    }

    //查询实体的粉丝数量
    public long findFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //查询某用户关注的人
    public List<User> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_User);
        Set<Integer> set = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (set == null) {
            return null;
        }
        List<User> list = new ArrayList<>();
        for (Integer integer : set) {
            list.add(userSerivice.findUserbyId(integer));
        }
        return list;

    }

    //查询某用户的粉丝
    public List<User> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_User, userId);
        Set<Integer> set = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (set == null) {
            return null;
        }
        List<User> list = new ArrayList<>();
        for (Integer integer : set) {
            list.add(userSerivice.findUserbyId(integer));
        }
        return list;

    }


}
