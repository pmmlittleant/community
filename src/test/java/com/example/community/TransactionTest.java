package com.example.community;

import com.example.community.service.AlphaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.PriorityQueue;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionTest {

    @Autowired
    AlphaService alphaService;

    @Test
    private void testSave1() {
        Object obj = alphaService.save1();
        System.out.println(obj);
    }

    @Test
    private void testSave2() {
        Object obj = alphaService.save2();
        System.out.println(obj);
    }
}
