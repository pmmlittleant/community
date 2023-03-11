package com.example.community.entity.event;

import com.alibaba.fastjson.JSONObject;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.Event;
import com.example.community.entity.Message;
import com.example.community.service.DiscussPostService;
import com.example.community.service.ElasticsearchService;
import com.example.community.service.MessageService;
import com.example.community.util.CommunityConstant;
import lombok.experimental.Accessors;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticsearchService elasticsearchService;



    @KafkaListener(topics = {TOPIC_LIKE, TOPIC_COMMENT,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息内容为空!");
            return;
        }
        //将以Json字符串格式发送的事件，转化成事件对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        //系统发送的消息message表中from_id 固定为1,将conversation_id该为存topic

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityId());
        message.setConversationId(event.getTopic());

        message.setCreateTime(new Date());

        // 构造message的内容 ("用户xxx(事件触发者userId）xxx(topic:点赞、关注、评论)了您(entityUserId)的xxx(entityType+entityId:帖子、评论等等)
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId()); // 事件触发者
        content.put("entityType", event.getEntityType()); //事件实体
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        //把内容map以JSON字符串存入message
        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }

    // 消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null) {
            logger.error("消息格式错误！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误！");
            return;
        }

        DiscussPost discussPost = discussPostService.findDiscussPostById(event.getEntityId());

        elasticsearchService.saveDiscussPost(discussPost);
    }
}
