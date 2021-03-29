package com.example.community.quartz;

import com.example.community.entity.DiscussPost;
import com.example.community.service.DiscussPostService;
import com.example.community.service.ElasticSearchService;
import com.example.community.service.LikeService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private LikeService likeService;

    private static final Date eopch;


    static {
        try {
            eopch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2021-02-16 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败! " + e.getMessage(), e);
        }
    }
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(postScoreKey);
        if (operations.size() == 0) {
            logger.info("任务取消，没有需要刷新的帖子");
            return;
        }

        logger.info("[任务开始] 正在刷新帖子分数：" + operations.size());
        while (operations.size() != 0) {
            refresh((int)operations.pop());
        }


        logger.info("[任务结束] 帖子分数刷新完毕");
    }


    private void refresh(int id) {
        DiscussPost discussPost = discussPostService.findDiscussPostById(id);
        if (discussPost == null) {
            logger.error("该帖子不存在：id=" + id);
            return;
        }

        boolean wonderful = discussPost.getStatus() == 1;
        int commentCount = discussPost.getCommentCount();
        int likeCount = (int)likeService.findEntityLikeCount(ENTIEY_TYPE_DISCUSSPOST, id);

        //计算权重
        double w = wonderful ? 75 : 0 + commentCount * 10 + likeCount * 2;

        double score = Math.log10(Math.max(1, w) + (discussPost.getCreateTime().getTime()
                - eopch.getTime() / (1000 * 3600 * 24))); //以天为单位

        //更新帖子分数
        discussPostService.updateScore(id, score);
        discussPost.setScore(score);
        elasticSearchService.sava(discussPost);

    }
}
