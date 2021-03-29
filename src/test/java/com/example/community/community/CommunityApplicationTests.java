package com.example.community.community;

import com.example.community.CommunityApplication;
import com.example.community.entity.Message;
import com.example.community.entity.User;
import com.example.community.mapper.DiscussPostMapper;
import com.example.community.mapper.LoginTicketMapper;
import com.example.community.mapper.MessageMapper;
import com.example.community.mapper.UserMapper;
import com.example.community.mapper.elasticsearch.DiscussPostRepository;
import com.example.community.service.CommentService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.MailClient;
import com.example.community.util.SensitiveFilter;
import com.google.code.kaptcha.Producer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;

import javax.mail.MessagingException;
import java.awt.image.BufferedImage;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class CommunityApplicationTests {


    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private SensitiveFilter sensitiveFilter;



    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private DiscussPostRepository discussPostRepository;



    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private CommentService commentService;




    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void contextLoads() {
        User user = new User();
        user.setUsername("1234");
        user.setPassword("88888888");
        userMapper.insertUser(user);
        System.out.println(userMapper.selectByName("1234"));
    }

    @Test
    public void UserMapperTest() {
//        User user = new User();
//        user.setHeaderUrl("123");
//        user.setStatus(1);
//        user.setPassword("888");
//        user.setUsername("12345678");
//        user.setEmail("18924789@asdh.com");
//        userMapper.insertUser(user);
//        System.out.println(userMapper.selectById(5));
//        System.out.println(userMapper.selectByName("12345678"));
//        System.out.println(userMapper.selectByEmail("18924789@asdh.com"));
//        userMapper.updateStatus(5, 666);
//        System.out.println(userMapper.selectById(5).getStatus());
//        userMapper.updateHeader(5, "456");
////        System.out.println(userMapper.selectById(5).getHeaderUrl());
        userMapper.updatePassword(5, "999");
        System.out.println(userMapper.selectById(5).getPassword());
    }


    @Test
    public void disscussPostTest() {
//        DiscussPost discussPost = new DiscussPost();
//        discussPost.setUserId(5);
//        discussPost.setTitle("Skhadsakjdh");
//        discussPost.setContent("sajkhdksjadh");
//        discussPostMapper.insertDisscussPost(discussPost);
//        System.out.println(discussPostMapper.selectDisscussPostRows(0));

    }


    @Test
    public void mailTest() throws MessagingException {
        String text = kaptchaProducer.createText();
        System.out.println(text);
        BufferedImage image = kaptchaProducer.createImage(text);
        System.out.println(image);
    }


    @Test
    public void loginTicketTest() {
        List<Message> messages = messageMapper.selectNotices(15, CommunityConstant.TOPIC_COMMENT, 0, 10);
        System.out.println(messages);

    }

    @Test
    public void elasticsearchTest() {
        System.out.println(sensitiveFilter.filter("我按时打卡是赌博撒德哈数据库的，wadh 大文豪嫖娼askdhsjkd"));

    }




}
