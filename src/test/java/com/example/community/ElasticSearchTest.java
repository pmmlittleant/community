package com.example.community;

import com.example.community.dao.DiscussPostMapper;
import com.example.community.dao.elasticsearch.DiscussPostRepository;
import com.example.community.entity.DiscussPost;
import com.example.community.entity.Page;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.profile.SearchProfileQueryPhaseResult;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

 //对于接口无法处理的功能，使用ElasticTemplate
    @Resource
    private ElasticsearchRestTemplate elasticTemplate;


    @Test
    public void testInsert() {
        discussRepository.save(discussPostMapper.selectDiscussPost(241));//添加一条数据
        discussRepository.save(discussPostMapper.selectDiscussPost(242));
        discussRepository.save(discussPostMapper.selectDiscussPost(243));

    }


    @Test
    public void testInsertList() {
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100));// 添加多条数据
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100));
        discussRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPost(231);
        post.setContent("我是新人，使劲灌水");
        discussRepository.save(post);
    }

    @Test
    public void testDelete() {
//        discussRepository.deleteById(231);
        discussRepository.deleteAll();
    }

    @Test
    public void testSearch() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content")) //在标题和内容两个字段中搜text
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC)) //按照类型倒叙排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();

        final SearchHits<DiscussPost> searchHits = elasticTemplate.search(searchQuery, DiscussPost.class);
        System.out.println(searchHits.getTotalHits());
        for (SearchHit<DiscussPost> hitpost : searchHits) {
            System.out.println(hitpost);
            System.out.println(hitpost.getHighlightFields().get("content"));
            System.out.println(hitpost.getHighlightFields().get("title"));

        }

    }
}
