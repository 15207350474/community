package com.example.community.service;


import com.example.community.entity.Comment;
import com.example.community.mapper.CommentMapper;
import com.example.community.mapper.DiscussPostMapper;
import com.example.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;


    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }


    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }



    //增加评论
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        //html标签过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        int row = commentMapper.insertComment(comment);

        if (comment.getEntityType() == CommunityConstant.ENTIEY_TYPE_DISCUSSPOST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());

            discussPostMapper.updateCommentCount(comment.getEntityId(), count);
        }
        return row;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }


    public List<Comment> findCommentsByUserId(int userId, int entityType, int offset, int limit) {
        return commentMapper.selectCommentsByUserId(userId, entityType, offset, limit);
    }



    public int findCountComment(int userId, int entityType) {
        return commentMapper.selectCountComment(userId, entityType);
    }
}
