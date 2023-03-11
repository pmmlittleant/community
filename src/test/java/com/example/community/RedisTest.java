package com.example.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;


    @Test
    public void testStrings() {
        String redisKey = "test:count";
        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));

    }

    @Test
    public void testHash() {
        String redisKey = "test:user";
        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "Eren");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    @Test
    public void testLists() {
        String redisKey = "test:ids";
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));

        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0 , 2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSet() {
        String redisKey = "test:teachers";
        redisTemplate.opsForSet().add(redisKey, "Eren", "Levi", "Armin", "Anni");
        System.out.println(redisTemplate.opsForSet().pop(redisKey));

        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    @Test
    public void testZSet() {
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "Eren", 100);
        redisTemplate.opsForZSet().add(redisKey, "Eren", 90);
        redisTemplate.opsForZSet().add(redisKey, "Levi", 80);
        redisTemplate.opsForZSet().add(redisKey, "Anni", 70);
        redisTemplate.opsForZSet().add(redisKey, "Armin", 60);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));

        System.out.println(redisTemplate.opsForZSet().score(redisKey, "Eren"));

        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "Eren"));
        System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 2));
    }

    @Test
    public void testKeys() {
        System.out.println(redisTemplate.delete("test:user"));
        System.out.println(redisTemplate.hasKey("test:user"));
        System.out.println(redisTemplate.expire("test:students", 10 , TimeUnit.SECONDS));
    }


    // 多次访问同一个Key
    @Test
    public void testBoundOperations() {
        String redisKey = "test:count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    @Test
    public void testTransaction() {
        final Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                operations.multi(); // 启动事务
                operations.opsForSet().add(redisKey, "Eren");
                operations.opsForSet().add(redisKey, "Armin");
                operations.opsForSet().add(redisKey, "Levi");

                System.out.println(operations.opsForSet().members(redisKey)); //[] 在事务中执行查询语句不会得到结果，无效
                return operations.exec(); //提交事务
            }
        });
        System.out.println(obj); //[1, 1, 1, [Levi, Eren, Armin]] 1，1，1为每个命令执行后修改的数据行数
    }
}
