package com.example.community.service;


import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 使用Redis实现对某实体（帖子，评论）的点赞功能（要考虑事务）（使用set类型）
 * 使用Redis实现查看某人获得的赞功能
 */

@Service
public class LikeService {


    @Autowired
    private RedisTemplate redisTemplate;


    //某人给某实体点赞 (已该实体为key，存储一个集合表示给该实体点赞的用户)
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        /*//获取某实体的key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        //判断某人是否给某实体点过赞
        Boolean member = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
        if (member) {
            //如果已经在该实体的key的集合中，说明已经点过赞，删除，取消点赞
            redisTemplate.opsForSet().remove(entityLikeKey, userId);
        } else {
            //如果不在,增加该用户，表示点赞
            redisTemplate.opsForSet().add(entityLikeKey, userId);
        }*/
        //两次数据库操作是关联的，要保证事务性，点赞的时候，被点赞的实体的作者点赞数要加1，同时要把点赞人的
        // id记录到被点赞的实体的点赞集合里
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //获取点赞实体的key, value存储的是给该实体点过赞的用户id
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                //获取点赞作者的key，value存储的是该作者被点赞的次数
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //判断该用户是否点赞过该实体
                Boolean member = redisOperations.opsForSet().isMember(entityLikeKey, userId);
                redisOperations.multi(); //开启事务
                if (member) { //如果已经点赞过，就是取消点赞
                    redisOperations.opsForSet().remove(entityLikeKey, userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                } else { //如果没点赞过，就是点赞
                    redisOperations.opsForSet().add(entityLikeKey, userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();
            }
        });
    }

    //查询某个用户获得赞的数量
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer)redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }

    //查询某实体点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    //查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }
}
