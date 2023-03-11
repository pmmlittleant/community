package com.example.community;

import com.example.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "这里可以@@赌@@@博，可以吸@@@毒，可以@嫖@@娼，可以@开@票，哈哈哈哈！";

        System.out.println(sensitiveFilter.filter(text));
    }
}
