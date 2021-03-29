package com.example.community.service;


import com.example.community.entity.DiscussPost;
import com.example.community.mapper.elasticsearch.DiscussPostRepository;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ElasticSearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate; //设置高亮


    public void sava(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    public void deleteDiscussPost(int id) {
        discussPostRepository.deleteById(id);
    }


    //从ElasticSearch服务器搜索帖子
    public Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) {
        //构造查询条件
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                //排序
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //分页查询
                .withPageable(PageRequest.of(current, limit))
                //高亮显示
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        return elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class,
                new SearchResultMapper() {
                    @Override
                    public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                        SearchHits hits = response.getHits();
                        if (hits.getTotalHits() <= 0) { //说明没查到数据
                            return null;
                        }
                        List<DiscussPost> list = new ArrayList<>();
                        for (SearchHit hit : hits) {
                            DiscussPost discussPost = new DiscussPost();

                            String id = hit.getSourceAsMap().get("id").toString();
                            discussPost.setId(Integer.valueOf(id));

                            String userId = hit.getSourceAsMap().get("userId").toString();
                            discussPost.setUserId(Integer.valueOf(userId));

                            String title = hit.getSourceAsMap().get("title").toString();
                            discussPost.setTitle(title);

                            String content = hit.getSourceAsMap().get("content").toString();
                            discussPost.setContent(content);

                            String status = hit.getSourceAsMap().get("status").toString();
                            discussPost.setStatus(Integer.valueOf(status));

                            String createTime = hit.getSourceAsMap().get("createTime").toString();
                            discussPost.setCreateTime(new Date(Long.valueOf(createTime)));

                            String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                            discussPost.setCommentCount(Integer.valueOf(commentCount));

                            //处理高亮显示的结果
                            HighlightField title1 = hit.getHighlightFields().get("title");
                            if (title1 != null) {
                                discussPost.setTitle(title1.getFragments()[0].toString());
                            }

                            HighlightField content1 = hit.getHighlightFields().get("content");
                            if (content1 != null) {
                                discussPost.setContent(content1.getFragments()[0].toString());
                            }
                            list.add(discussPost);

                        }
                        return new AggregatedPageImpl(list, pageable, hits.getTotalHits(), response.getAggregations()
                                , response.getScrollId(), hits.getMaxScore());
                    }
                });
    }


}
