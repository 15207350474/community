package com.example.community.mapper;


import com.example.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    //分页根据实体查询评论
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    List<Comment> selectCommentsByUserId(int userId, int entityType, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    int selectCountComment(int userId, int entityType);
}
