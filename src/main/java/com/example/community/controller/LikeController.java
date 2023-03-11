package com.example.community.controller;

import com.example.community.entity.Event;
import com.example.community.entity.User;
import com.example.community.entity.event.EventProducer;
import com.example.community.service.LikeService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;


    @RequestMapping(value = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entity_type, int entity_id, int entity_user_id, int postId) {
        User user = hostHolder.getUser();

        // 点赞
        likeService.like(user.getId(), entity_type, entity_id, entity_user_id);
        // 数量
        long likeCount = likeService.findEntityLikeCount(entity_type, entity_id);
        // 状态
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entity_type,entity_id);

        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件
        if (likeStatus == 1) {
            Event event = new Event();
            event
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entity_type)
                    .setEntityId(entity_id)
                    .setEntityUserId(entity_user_id)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }
}
