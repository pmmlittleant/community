package com.example.community.controller;

import com.example.community.entity.DiscussPost;
import com.example.community.entity.Page;
import com.example.community.service.ElasticsearchService;
import com.example.community.service.LikeService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {


    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // search?keyword=xxx
    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        // 搜索帖子
       SearchHits<DiscussPost> searchHits = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());
       // 聚合数据
       List<Map<String, Object>> discussPosts = new ArrayList<>();

       if (searchHits != null) {
           for (SearchHit<DiscussPost> searchHit : searchHits) {
               Map<String, Object> map = new HashMap<>();

               // 帖子
               DiscussPost post = searchHit.getContent();
               map.put("post", post);
               // 作者
               map.put("user", userService.findUserById(post.getUserId()));
               // 点赞数量
               map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

               discussPosts.add(map);
           }
       }
       model.addAttribute("discussPosts", discussPosts);
       model.addAttribute("keyword", keyword);

       // 分页信息
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchHits == null?0: (int) searchHits.getTotalHits());

        return "/site/search";
    }




}
