package com.example.community.mapper;


import com.example.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    //查询当前用户会话列表，对每个会话只返回最新的一个
    List<Message> selectCoversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int selectCoversationCount(int userId);

    //查询一个会话所有私信
    List<Message> selectLetters(String coversationId, int offset, int limit);

    //查询某个会话包含的私信数量
    int selectLetterCount(String coversationId);

    //查询当前用户所有未读私信数量
    int selectLetterUnreadCount(int userId, String coversationId);

    //新增私信
    int insertMessage(Message message);

    //更改私信状态
    int updateStatus(List<Integer> ids, int status);


    //查询某个主题下最新的一条通知
    Message selectLastNotice(int userId, String topic);

    //查询某个主题包含通知的数量
    int selectNoticeCount(int userId, String topic);

    //查询未读的通知数量
    int selectNoticeUnreadCount(int userId, String topic);

    //查询某一个主题包含的所有通知
    List<Message> selectNotices(int userId, String topic, int offset, int limit);



}
