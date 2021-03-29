package com.example.community.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data

// @Document指定当前类是索引对象。indexName:索引名称;shards:创建索引时的分片数;replicas:创建索引时的备份数
@Document(indexName = "discusspost", type = "_doc", shards = 5, replicas = 1) //elasticsearch 实体类对应的索引
public class DiscussPost {
    @Id
    private int id;
    @Field(type = FieldType.Integer)
    private int userId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    @Field(type = FieldType.Integer)
    private int commentCount;
    @Field(type = FieldType.Date)
    private Date createTime;
    @Field(type = FieldType.Integer)
    private int status;
    @Field(type = FieldType.Double)
    private double score;
    @Field(type = FieldType.Integer)
    private int type;
}
