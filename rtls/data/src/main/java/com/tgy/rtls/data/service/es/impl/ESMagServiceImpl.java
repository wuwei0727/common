package com.tgy.rtls.data.service.es.impl;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.es.ESMag;
import com.tgy.rtls.data.service.es.ESMagService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.service.ES.impl
 * @Author: wuwei
 * @CreateTime: 2023-10-20 16:46
 * @Description: TODO
 * @Version: 1.0
 */
@Service
@Slf4j
public class ESMagServiceImpl {
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ElasticsearchOperations operations;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private ESMagService magService;

    public void addMag(ESMag mag){
        magService.save(mag);
    }

    public List<ESMag> searchMag(String num,Integer state, String startTime, String endTime) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!NullUtils.isEmpty(num)) {
            boolQuery.must(QueryBuilders.termQuery("num", num));
        }

        if (state != null) {
            boolQuery.must(QueryBuilders.termQuery("state", state));
        }
        if (!NullUtils.isEmpty(startTime) && !NullUtils.isEmpty(endTime)) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("time")
                    .gte(startTime)
                    .lte(endTime);
            boolQuery.must(rangeQuery);
        }

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .build();
        SearchHits<ESMag> searchHits = elasticsearchRestTemplate.search(searchQuery, ESMag.class);

        // Calculate time difference for each data object
        return searchHits.stream()
                .map(SearchHit::getContent)
                .sorted(Comparator.comparing(ESMag::getTime))  // Sort by timestamp in descending order
                .collect(Collectors.toList());
    }
}
