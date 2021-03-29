package com.example.community.service;


import com.example.community.entity.DiscussPost;
import com.example.community.mapper.DiscussPostMapper;
import com.example.community.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    /*@Value("${caffeine.posts.max.size}")
    private int maxSize;*/

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    /*//热门帖子变动频率小，查询频率高，可以缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    //帖子总数缓存（帖子的页数每次访问帖子列表都要查询，数据延迟一些影响也不大，所有可以缓存）
    private LoadingCache<Integer, Integer> postRowsCache;*/

    @Autowired
    private DiscussPostMapper discussPostMapper;


    @Autowired
    private RedisTemplate redisTemplate;

    /*@PostConstruct //在类调用的时候初始化缓存，只会初始化一次
    public void init() {
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String s) throws Exception {
                        if (s == null || s.length() == 0) {
                            throw new IllegalArgumentException("参数为空！");
                        }
                        String[] params = s.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }
                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        logger.debug("load post list from DB");
                        return discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });

        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer integer) throws Exception {
                        logger.debug("load post rows from DB");
                        return discussPostMapper.selectDiscussPostRows(0);
                    }
                });
    }*/


    //分页返回指定用户iD的帖子或所有帖子
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit, int order) {
        if (userId == 0 && order == 1) {
            return getHotPostFromCache(offset, limit);
        }
        logger.debug("load post list from DB");
        return discussPostMapper.selectDiscussPosts(userId, offset, limit, order);
    }

    //分页返回指定用户ID的帖子数量或全部帖子数量
    public int findDiscussPostRows(int userId) {
        if (userId == 0) { //查询自己个帖子不需要缓存
            return getPostRowsFromCache();
        }
        logger.debug("load post rows from DB");
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    //缓存热门帖子
    public List<DiscussPost> getHotPostFromCache(int offset, int limit) {
        String hotPostKey = RedisKeyUtil.getHotPostKey();
        List<DiscussPost> list = (List<DiscussPost>) redisTemplate.opsForValue().get(hotPostKey);
        if (list == null) {
            list =  discussPostMapper.selectDiscussPosts(0, offset, limit, 1);
            redisTemplate.opsForValue().set(hotPostKey, list, expireSeconds, TimeUnit.SECONDS);
            logger.info("从缓存数据库中查询热门帖子");
        }
        return list;
    }

    //缓存帖子行数
    public int getPostRowsFromCache() {
        String postRowsKey = RedisKeyUtil.getPostRowsKey();
        Integer rows = (Integer) redisTemplate.opsForValue().get(postRowsKey);
        if (rows == null) {
            rows = discussPostMapper.selectDiscussPostRows(0);
            redisTemplate.opsForValue().set(postRowsKey, rows, expireSeconds, TimeUnit.SECONDS);
            logger.info("从数据库中查询帖子行数");
        }
        return rows;
    }









    //增加一条帖子
    public int addDiscussPost(DiscussPost discussPost) {
        if (discussPost == null) throw new IllegalArgumentException("参数不能为空");
        //转义HTML标签
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));

        //过滤敏感词


        return discussPostMapper.insertDiscussPost(discussPost);
    }


    //根据帖子id查询一条帖子
    public DiscussPost findDiscussPostById(int id) {
        return discussPostMapper.selectDiscussPostById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }


    public int updateScore(int id, double score) {
        return discussPostMapper.updateScore(id, score);
    }


}
