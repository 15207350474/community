package com.example.community.mapper;

import com.example.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface DiscussPostMapper {

    //分页查询指定用户id帖子或者全部帖子(动态SQL, 当userId=0时，查询所有帖子)
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);


    //@Param注解用于给参数取别名，如果只有一个参数，并且要使用<if>动态SQL,则必须取别名
    //查询查询指定用户id帖子条数或全部帖子条数(动态SQL，当userID=0时，查询所有帖子条数)
    int selectDiscussPostRows(@Param("userId") int userId); //动态查询如果只有一个条件，一定要取别名


    //保存一条帖子
    int insertDiscussPost(DiscussPost discussPost);


    //根据帖子id查询帖子
    DiscussPost selectDiscussPostById(int id);

    //更新帖子评论数量
    int updateCommentCount(int id, int commentCount);

    //更改帖子状态
    int updateStatus(int id, int status);

    //更改帖子类型
    int updateType(int id, int type);

    //更新帖子分数
    int updateScore(int id, double score);
}
