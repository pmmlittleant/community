package com.example.community.service;

import com.example.community.dao.elasticsearch.DiscussPostRepository;
import com.example.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Resource
    private ElasticsearchRestTemplate elasticTemplate;

    public void saveDiscussPost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }


    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }


    public SearchHits<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        // 构造查询条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content")) //在标题和内容两个字段中搜text
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC)) //按照类型倒叙排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                )
                .build();

        final SearchHits<DiscussPost> searchHits = elasticTemplate.search(searchQuery, DiscussPost.class);
        if (searchHits.getTotalHits() <= 0) return null;
        return searchHits;
    }
}
