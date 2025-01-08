package com.tgy.rtls.data.service.es.impl;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.common.TimeUtil;
import com.tgy.rtls.data.entity.es.ESInfraredOriginal;
import com.tgy.rtls.data.service.es.ESInfraredOriginalService;
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

import java.util.*;
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
public class ESInfraredOriginalImpl {
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ElasticsearchOperations operations;;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private ESInfraredOriginalService esInfraredOriginalService;

    public void addOriginal(ESInfraredOriginal original){
        esInfraredOriginalService.save(original);
    }

    public List<ESInfraredOriginal> searchInfraredOriginal(String gatewayNum, String infraredNum, Integer state, String startTime, String endTime) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!NullUtils.isEmpty(gatewayNum)) {
            boolQuery.must(QueryBuilders.termQuery("gatewayNum", gatewayNum));
        }
        if (!NullUtils.isEmpty(infraredNum)) {
            boolQuery.must(QueryBuilders.termQuery("infraredNum", infraredNum));
        }
        if (state != null) {
            boolQuery.must(QueryBuilders.termQuery("state", state));
        }
        if (!NullUtils.isEmpty(startTime) && !NullUtils.isEmpty(endTime)) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("timestamp")
                    .gte(startTime)
                    .lte(endTime);
            boolQuery.must(rangeQuery);
        }
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .build();

        SearchHits<ESInfraredOriginal> searchHits = elasticsearchRestTemplate.search(searchQuery, ESInfraredOriginal.class);
        // Count occurrences of each count value
        Map<Integer, Integer> countOccurrences = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.groupingBy(ESInfraredOriginal::getCount,Collectors.summingInt(it -> 1)));

        // Convert SearchHits to a List<ESInfraredOriginal>
        List<ESInfraredOriginal> list1 = searchHits.stream()
                .map(SearchHit::getContent)
                .sorted(Comparator.comparing(ESInfraredOriginal::getTimestamp))  // Sort by timestamp in descending order
                .collect(Collectors.toList());
        // Set appearCount for the last data object
        if (!list1.isEmpty()) {
            ESInfraredOriginal lastESInfraredOriginal = list1.get(list1.size() - 1);
            Integer count = Math.toIntExact(countOccurrences.getOrDefault(lastESInfraredOriginal.getCount(), 0));
            lastESInfraredOriginal.setAppearCount(count);
        }
        // Calculate time difference for each data object
        return list1.stream()
                .peek(currentData -> {
                    Date currentTime = TimeUtil.parseTimestamp(currentData.getTimestamp());

                    int index = list1.indexOf(currentData);
                    if (index > 0) {
                        ESInfraredOriginal previousData = list1.get(index - 1);
                        Date previousTime = TimeUtil.parseTimestamp(previousData.getTimestamp());
                        int timeDifference = (int) ((Objects.requireNonNull(currentTime).getTime() - Objects.requireNonNull(previousTime).getTime()) / 1000);
                        currentData.setTimePoor(timeDifference);
                    } else {
                        currentData.setTimePoor(0);
                    }

                })
                .collect(Collectors.toList());

        // return list1;
    }
}
