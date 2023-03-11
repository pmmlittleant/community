package com.example.community.dao.elasticsearch;

import com.example.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository //Spring提供的针对数据访问层的类的注解
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> { //声明接口处理的实体类，以及主键类型

}
