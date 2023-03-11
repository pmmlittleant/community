package com.example.community;

import com.example.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "张三");
        final String content = templateEngine.process("/mail/demo", context);

        System.out.println(content);

        mailClient.sendMail("astrid2324@163.com", "testHtml", content);
     }


    @Test
    public void testMail() {
        mailClient.sendMail("astrid2324@163.com", "test", "welcome!");
    }

}
