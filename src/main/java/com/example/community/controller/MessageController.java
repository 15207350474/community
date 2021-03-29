package com.example.community.controller;


import com.alibaba.fastjson.JSONObject;
import com.example.community.entity.Message;
import com.example.community.entity.Page;
import com.example.community.entity.User;
import com.example.community.service.MessageService;
import com.example.community.service.UserSerivice;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {


    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;


    @Autowired
    private UserSerivice userSerivice;


    //查询私信列表
    @GetMapping("/letter/list")
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findCoversationCount(user.getId()));
        List<Message> coversationList = messageService.findCoversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> coversations = new ArrayList<>();

        if (coversationList != null) {
            for (Message message : coversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                map.put("letterCount", messageService.findLetterCount(message.getCoversationId()));
                map.put("letterUnreadCount", messageService.findLetterUnreadCount(user.getId(), message.getCoversationId()));
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userSerivice.findUserbyId(targetId));
                coversations.add(map);
            }
        }
        model.addAttribute("coversations", coversations);
        model.addAttribute("letterUnreadCount", messageService.findLetterUnreadCount(user.getId(), null));
        model.addAttribute("noticeUnreadCount", messageService.findNoticeUnreadCount(user.getId(), null));
        return "/site/letter";
    }

    //查询私信详情
    @GetMapping("/letter/detail/{coversationId}")
    public String getLetterDetail(@PathVariable("coversationId") String coversationId, Page page, Model model) {

        page.setPath("/letter/detail/" + coversationId);
        page.setLimit(10);
        page.setRows(messageService.findLetterCount(coversationId));

        List<Message> letters = messageService.findLetters(coversationId, page.getOffset(), page.getLimit());
        User user = hostHolder.getUser();
        String[] s = coversationId.split("_");
        User fromUser = user.getId() == Integer.parseInt(s[0]) ? userSerivice.findUserbyId(Integer.parseInt(s[1])) :
                userSerivice.findUserbyId(Integer.parseInt(s[1]));

        List<Map<String, Object>> list = new ArrayList<>();

        if (letters != null) {
            for (Message letter : letters) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                map.put("fromUser", userSerivice.findUserbyId(letter.getFromId()));
                list.add(map);
            }
        }
        model.addAttribute("letters", list);
        model.addAttribute("fromUser", fromUser);

        //设置已读
       messageService.setread(letters, user.getId());

        return "/site/letter-detail";
    }


    //发送私信
    @PostMapping("/letter/send")
    @ResponseBody
    public String sendLetter(String content, String toName) {
        User target = userSerivice.findByUserName(toName);
        if (target == null) {
            return CommunityUtil.getJsonString(1, "发送失败，目标用户不存在");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        message.setContent(content);

        if (message.getFromId() < message.getToId()) {
            message.setCoversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setCoversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setCreateTime(new Date());

        messageService.addMessage(message);

        return CommunityUtil.getJsonString(0, "消息发送成功");
    }


    //查询通知列表
    @GetMapping("/notice/list")
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();
        //查询评论类通知
        Message message = messageService.findLastNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        if (message != null) {
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap map1 = JSONObject.parseObject(content, HashMap.class);
            map.put("user", userSerivice.findUserbyId((Integer)map1.get("userId")));
            map.put("entityType", map1.get("entityType"));
            map.put("entityId", map1.get("entityId"));
            map.put("discussPostId", map1.get("discussPostId")); //需要帖子Id

            map.put("unRead", messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT));
            map.put("count", messageService.findNoticeCount(user.getId(), TOPIC_COMMENT));
        }
        model.addAttribute("commentNotice", map);

        //查询点赞类通知
        message = messageService.findLastNotice(user.getId(), TOPIC_LIKE);
        map = new HashMap<>();
        map.put("message", message);
        if (message != null) {
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap map1 = JSONObject.parseObject(content, HashMap.class);
            map.put("user", userSerivice.findUserbyId((Integer)map1.get("userId")));
            map.put("entityType", map1.get("entityType"));
            map.put("entityId", map1.get("entityId"));
            map.put("discussPostId", map1.get("discussPostId")); //需要帖子Id

            map.put("unRead", messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE));
            map.put("count", messageService.findNoticeCount(user.getId(), TOPIC_LIKE));
        }
        model.addAttribute("likeNotice", map);

        //查询关注类通知
        message = messageService.findLastNotice(user.getId(), TOPIC_FOLLOW);
        map = new HashMap<>();
        map.put("message", message);
        if (message != null) {
            String content = HtmlUtils.htmlUnescape(message.getContent());
            HashMap map1 = JSONObject.parseObject(content, HashMap.class);
            map.put("user", userSerivice.findUserbyId((Integer)map1.get("userId")));
            map.put("entityType", map1.get("entityType"));
            map.put("entityId", map1.get("entityId"));

            map.put("unRead", messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW));
            map.put("count", messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW));
        }
        model.addAttribute("followNotice", map);

        model.addAttribute("letterUnreadCount", messageService.findLetterUnreadCount(user.getId(), null));
        model.addAttribute("noticeUnreadCount", messageService.findNoticeUnreadCount(user.getId(), null));

        return "/site/notice";
    }

    //查看通知详情
    @GetMapping("/notice/detail/{topic}")
    public String getNotices(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setRows(messageService.findNoticeCount(user.getId(), topic));
        page.setPath("/notice/detail/" + topic);
        page.setLimit(6);

        List<Message> notices = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (notices != null) {
            for (Message notice : notices) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);

                String content = HtmlUtils.htmlUnescape(notice.getContent());
                HashMap map1 = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userSerivice.findUserbyId((Integer)map1.get("userId")));
                map.put("entityId", map1.get("entityId"));
                map.put("entityType", map1.get("entityType"));
                map.put("discussPostId", map1.get("discussPostId"));

                list.add(map);
            }
        }
        model.addAttribute("notices", list);
        //设置已读
        messageService.setread(notices, user.getId());

        return "/site/notice-detail";
    }



}
