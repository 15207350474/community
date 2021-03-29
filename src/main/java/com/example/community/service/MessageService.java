package com.example.community.service;


import com.example.community.entity.Message;
import com.example.community.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    public List<Message> findCoversations(int userId, int offset, int limit) {
        return messageMapper.selectCoversations(userId, offset, limit);
    }

    public int findCoversationCount(int userId) {
        return messageMapper.selectCoversationCount(userId);
    }

    public List<Message> findLetters(String coversationId, int offset, int limit) {
        return messageMapper.selectLetters(coversationId, offset, limit);
    }

    public int findLetterCount(String coversationId) {
        return messageMapper.selectLetterCount(coversationId);
    }

    public int findLetterUnreadCount(int userId, String coversationId) {
        return messageMapper.selectLetterUnreadCount(userId, coversationId);
    }

    public int addMessage(Message message) {
        return messageMapper.insertMessage(message);
    }

    public int readMessage(List<Integer> ids) {
        return messageMapper.updateStatus(ids, 1);
    }


    //查询某个主题下的最新的一条通知
    public Message findLastNotice(int userId, String topic) {
        return messageMapper.selectLastNotice(userId, topic);
    }
    //查询某个主题下或者所有主题的未读通知数量
    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    //查询某个主题里的所有的通知数量
    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    //查询某个主题里的所有的通知
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }


    //设置已读
    public void setread(List<Message> messages, int userId) {
        List<Integer> ids = new ArrayList<>();
        for (Message message : messages) {
            if (message.getStatus() == 0 && userId == message.getToId()) {
                ids.add(message.getId());
            }
        }
        if (!ids.isEmpty()) {
            messageMapper.updateStatus(ids, 1);
        }

    }





}
