package com.example.community.controller;

import com.example.community.entity.Comment;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.Event;
import com.example.community.entity.event.EventProducer;
import com.example.community.service.CommentService;
import com.example.community.service.DiscussPostService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.HostHolder;
import org.apache.catalina.Host;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private DiscussPostService discussPostService;

    @RequestMapping(value = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        // 触发评论事件
        Event event = new Event();
        event
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);

        //如果评论对象是帖子，存入帖子的作者ID
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
           DiscussPost target = discussPostService.findDiscussPostById(comment.getEntityId());
           event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT){
            //如果评论对象是评论，存入评论的作者Id
            Comment target = commentService.findCommentById(comment.getEntityId());
            event.setEntityId(target.getUserId());
        }
        // 通过生产者发布事件
        eventProducer.fireEvent(event);

        return "redirect:/discuss/detail/" + discussPostId;
    }
}
