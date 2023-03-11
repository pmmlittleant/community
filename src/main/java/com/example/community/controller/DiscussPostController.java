package com.example.community.controller;

import com.example.community.entity.*;
import com.example.community.entity.event.EventProducer;
import com.example.community.service.CommentService;
import com.example.community.service.DiscussPostService;
import com.example.community.service.LikeService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.sql.SQLOutput;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;


    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String content, String title){
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录!"); // 403 无权限
        }
        System.out.println(title);
        System.out.println(content);
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 触发发帖事件
        Event event = new Event();
        event
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());

        eventProducer.fireEvent(event);


        //报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功");
    }

    @GetMapping("/detail/{discussPostId}")
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId,
                                 Model model,
                                 Page page) { //只要是实体类型，作为controller方法参数的话，SpingMVC都会把对象存入到model中，页面可以从model中获取对象
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        //帖子的信息中需要显示用户信息而不是post里的userid，所以我们还需要做一次User的查询
        // 而两次查询可能影响服务器的效率，所以，后面可以用Redis来解决，把用户存在内存中

        //作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //点赞数
        model.addAttribute("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId));

        //点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);

        model.addAttribute("likeStatus", likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        final List<Comment> commentList = commentService.
                findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffSet(), page.getLimit());

        // 评论：给帖子的评论
        // 回复：给评论的评论
        // 评论列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();

        //评论VO列表（view object）
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                //点赞数
                long liKeCount =  likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", liKeCount);
                //点赞状态
               likeStatus = hostHolder.getUser() == null ? 0 :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());

                commentVo.put("likeStatus", likeStatus);



                // 回复列表
                final List<Comment> replyList = commentService.
                        findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);

                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        // 回复Vo
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", userService.findUserById(reply.getUserId()));


                        //点赞数
                       liKeCount =  likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", liKeCount);
                        //点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());

                        replyVo.put("likeStatus", likeStatus);



                        // 回复目标 （target_id)
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replies", replyVoList);

                // 回复数量
                commentVo.put("replyCount", replyList.size());

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }
}
