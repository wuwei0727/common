package com.tgy.rtls.web.controller.ES;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.es.park.EsCompany;
import com.tgy.rtls.web.util.ElasticsearchUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.ES
 * @Author: wuwei
 * @CreateTime: 2024-06-28 15:58
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping(value = "/es")
@CrossOrigin
@Slf4j
public class EsSearchController {
    @Resource
    private RestHighLevelClient client;
    @Resource
    private ElasticsearchUtil elasticsearchUtil;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/search")
    public CommonResult<Object> searchArticles(@RequestParam String keyword, @RequestParam String indexName, @RequestParam Integer map, @RequestParam(defaultValue = "0") Integer from, @RequestParam(defaultValue = "10000") Integer size) throws IOException {
        SearchRequest searchRequest = new SearchRequest(indexName);
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.matchQuery("name", keyword));
        boolQuery.must(QueryBuilders.termQuery("map", map));
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder().from(from).size(size);
        sourceBuilder.query(boolQuery);

        searchRequest.source(sourceBuilder);
        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), elasticsearchUtil.convertSearchResponse(searchRequest, EsCompany.class));
//        List<EsCompany> companies = new ArrayList<>();
//        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        //        searchResponse.getHits().forEach(hit -> {
//            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
//            EsCompany company = new EsCompany();
//            company.setId(Long.valueOf(sourceAsMap.get("id").toString()));
//            company.setName((String) sourceAsMap.get("name"));
//            company.setUser((String) sourceAsMap.get("user"));
//            company.setPwd((String) sourceAsMap.get("pwd"));
//            company.setPhone((String) sourceAsMap.get("phone"));
//            company.setRole((String) sourceAsMap.get("role"));
//            company.setFloor((String) sourceAsMap.get("floor"));
//            company.setX((String) sourceAsMap.get("x"));
//            company.setY((String) sourceAsMap.get("y"));
//            company.setFid((String) sourceAsMap.get("fid"));
//            company.setMap((String) sourceAsMap.get("map"));
//            String addTimeStr = (String) sourceAsMap.get("addTime");
//            if (addTimeStr != null) {
//                company.setAddTime(LocalDateTime.parse(addTimeStr, DATE_TIME_FORMATTER));
//            }
////            company.setMapName((String) sourceAsMap.get("mapName"));
////            company.setFloorName((String) sourceAsMap.get("floorName"));
//            companies.add(company);
//        });
//        return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS),companies);
    }
    //这是一个main方法，程序的入口
    public static void main(String[] args) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 解析Kafka消息中的时间戳
        long realTimeTimestamp = dateFormat.parse("2024-07-03 09:07:20").getTime();

        // 获取当前时间的时间戳
        long currentTime = System.currentTimeMillis();

        // 计算30分钟的毫秒数
        long thirtyMinutesInMillis = 30 * 60 * 1000;

        // 判断realTimeTimestamp是否在当前时间的30分钟内
        if (currentTime - realTimeTimestamp <= thirtyMinutesInMillis) {
            // 时间在当前时间的30分钟内，处理消息
            System.out.println("时间在当前时间的30分钟内");

        } else {
            // 时间不在当前时间的30分钟内，忽略消息或进行其他处理
            System.out.println("消息时间不在当前时间的30分钟内");
        }
    }
}
