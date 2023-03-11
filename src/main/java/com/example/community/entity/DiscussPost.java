package com.example.community.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@ToString
@Document(indexName = "discusspost", shards = 6, replicas = 3) //Spring整合ES，会自动将实体属性映射到ES的字段
public class DiscussPost {

    @Id
    private int id;

    @Field(type = FieldType.Integer)
    private int userId;

    // 互联网校招， analyzer为分词器，存储时使用最多的分词器，搜索时使用聪明的分词器
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    @Field(type = FieldType.Integer)
    private int type; // 0-普通; 1-置顶
    @Field(type = FieldType.Integer)
    private int status; //'0-正常; 1-精华; 2-拉黑;

    @Field(type = FieldType.Date)
    private Date createTime;

    @Field(type = FieldType.Integer)
    private int commentCount;

    @Field(type = FieldType.Double)
    private double score;
}
