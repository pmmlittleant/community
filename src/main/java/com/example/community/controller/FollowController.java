package com.example.community.controller;

import com.example.community.annotation.LoginRequired;
import com.example.community.entity.Event;
import com.example.community.entity.Page;
import com.example.community.entity.User;
import com.example.community.entity.event.EventProducer;
import com.example.community.service.FollowService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;



@Controller
public class FollowController implements CommunityConstant{
    @Autowired
    private FollowService followService;
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;


    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    @ResponseBody     //处理关注和取消关注的异步AJAX请求
    @LoginRequired
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        // 触发关注事件
        Event event = new Event();
        event
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId); // 目前的关注功能只能关注人
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0, "已关注");
    }

    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    @ResponseBody     //处理关注和取消关注的异步AJAX请求
    @LoginRequired
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unsubscribe(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注");
    }

    @RequestMapping(value = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/"+ userId);
        page.setRows((int)followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        final List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffSet(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);
        return "/site/followee";
    }

    @RequestMapping(value = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/"+ userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        final List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffSet(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));

            }
        }
        model.addAttribute("users", userList);
        return "/site/follower";
    }

    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}
