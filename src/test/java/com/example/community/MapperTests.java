package com.example.community;


import com.example.community.dao.DiscussPostMapper;
import com.example.community.dao.LoginTicketMapper;
import com.example.community.dao.MessageMapper;
import com.example.community.dao.UserMapper;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.LoginTicket;
import com.example.community.entity.Message;
import com.example.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper postMapper;
    @Autowired
    private LoginTicketMapper ticketMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        final User user = userMapper.selectByName("liubei");
        System.out.println(user);

        System.out.println(userMapper.selectById(101));
        System.out.println(userMapper.selectByEmail("nowcoder101@sina.com"));

    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());

    }

    @Test
    public void testUpdateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");

        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Test
    public void testDiscussPost() {
        final List<DiscussPost> list = postMapper.selectDiscussPosts(149, 0, 10);
        for (DiscussPost post : list) {
            System.out.println(post);
        }

        final int rows = postMapper.selectDiscussPostRows(149);
        System.out.println(rows);
    }

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        final int i = ticketMapper.insertLoginTicket(loginTicket);
        System.out.println(i);
        System.out.println(loginTicket.getId());
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = ticketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
        ticketMapper.updateStatus("abc", 1);
        loginTicket = ticketMapper.selectByTicket("abc");
        System.out.println(loginTicket);
    }

    @Test
    public void testSelectMessage() {
        final List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message m : messages) {
            System.out.println(m);
        }

        System.out.println(messageMapper.selectConversationCount(111));

        messageMapper.selectLetters("111_112", 0, 10).forEach(System.out::println);

        System.out.println(messageMapper.selectLetterCount("111_112"));

        System.out.println(messageMapper.selectLetterUnreadCount(131, "111_131"));
    }
}
