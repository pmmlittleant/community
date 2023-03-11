package com.example.community.entity.event;

import com.alibaba.fastjson.JSONObject;
import com.example.community.entity.Event;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class EventProducer {
    @Resource
    private KafkaTemplate kafkaTemplate;

    // 处理事件 （本质是发消息）
    public void fireEvent(Event event) {
        // 将事件发布到指定的主题
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event)); //将事件以json字符串格式发送
    }
}
