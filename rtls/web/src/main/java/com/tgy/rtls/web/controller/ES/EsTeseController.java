package com.tgy.rtls.web.controller.ES;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.common.TimeUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.es.*;
import com.tgy.rtls.data.mapper.ES.EsMagMapper;
import com.tgy.rtls.data.mapper.ES.EsTestMapper;
import com.tgy.rtls.data.service.es.ESInfraredOriginalService;
import com.tgy.rtls.data.service.es.impl.ESInfraredOriginalImpl;
import com.tgy.rtls.data.service.es.impl.ESMagServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import org.elasticsearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.ES
 * @Author: wuwei
 * @CreateTime: 2023-10-16 17:33
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping (value = "/es")
@CrossOrigin
@Slf4j
public class EsTeseController {
    @Autowired
    private ESInfraredOriginalService esBookRepository;
    @Autowired
    private EsTestMapper esTestMapper;
    @Autowired
    private EsMagMapper magMapper;
    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ElasticsearchOperations operations;;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private ESInfraredOriginalImpl esInfraredOriginalImpl;
    @Autowired
    private ESMagServiceImpl magService;


    @RequestMapping(value = "/getMag")
    public CommonResult<Object> getMag(String num, Integer state,String startTime, String endTime){
        List<ESMag> mags = magService.searchMag(num, state, startTime, endTime);
        return new CommonResult<>(200, "success", mags);
    }
    @RequestMapping(value = "/addMag")
    public void addMag() {
        ESMag mag=new ESMag();
        mag.setNum("1");
        mag.setTime(TimeUtil.localDateTimeToStrTime(LocalDateTime.now()));
        mag.setX("1.1");
        mag.setY("1.1");
        mag.setZ("1.1");
        mag.setX_fix("1.1");
        mag.setY_fix("1.1");
        mag.setZ_fix("1.1");
        mag.setX_diff("1.1");
        mag.setY_diff("1.1");
        mag.setZ_diff("1.1");
        mag.setOccupy_x("1.1");
        mag.setOccupy_y("1.1");
        mag.setOccupy_z("1.1");
        mag.setEmpty_x("1.1");
        mag.setEmpty_y("1.1");
        mag.setEmpty_z("1.1");
        mag.setState(0);
        magService.addMag(mag);
    }


    @PostMapping("/addMag")
    public Object addMag(ESMag mag) throws IOException {
        mag.setNum("1");
        mag.setTime(TimeUtil.localDateTimeToStrTime(LocalDateTime.now()));
//        mag.setTime("2024-11-01 20:23:56");
        mag.setX("1.1");
        mag.setY("1.1");
        mag.setZ("1.1");
        mag.setX_fix("1.1");
        mag.setY_fix("1.1");
        mag.setZ_fix("1.1");
        mag.setX_diff("1.1");
        mag.setY_diff("1.1");
        mag.setZ_diff("1.1");
        mag.setOccupy_x("1.1");
        mag.setOccupy_y("1.1");
        mag.setOccupy_z("1.1");
        mag.setEmpty_x("1.1");
        mag.setEmpty_y("1.1");
        mag.setEmpty_z("1.1");
        mag.setState(0);
        //获取类上的注解
        // 1、 获取 User类上的注解 @ConsAnnotation
        Class<ESMag> clazz = ESMag.class;
        // 检查类上是否存在@Document注解
        if (clazz.isAnnotationPresent(Document.class)) {
            Document documentAnnotation = clazz.getAnnotation(Document.class);
            // 获取@Document注解的indexName值
            String indexName = documentAnnotation.indexName();
            if(!NullUtils.isEmpty(indexName)){
                if (getIndex(indexName)) {
                    elasticsearchRestTemplate.indexOps(ESInfraredOriginal.class).putMapping(operations.indexOps(ESMag.class).createMapping());
                    return magMapper.save(mag);
                }else {
                    if(createIndex(EsConst.mag,ESMag.class)){
                        return magMapper.save(mag);
                    }
                }
            }
        } else {
            log.error("addOriginal:No @Document annotation found on the class");

            return false;
        }
            return false;
    }

    @GetMapping ("/addInfraredOriginal")
    public Object addInfraredOriginal(String time) throws IOException {
//        for (int i = 0; i <= count; i++) {
            ESInfraredOriginal original = new ESInfraredOriginal();
            original.setTimestamp(time);
            original.setGatewayNum("1000");
            original.setInfraredNum("210");
            original.setState(0);
            original.setCount(1111111);
            original.setRssi(22);
            original.setPower(0);

//        }
        esInfraredOriginalImpl.addOriginal(original);
        return esTestMapper.count();
    }

    @GetMapping ("/book")
    public Object addBook(Integer count,String mapName) throws IOException {
        // List<findcartest> list = new LinkedList<>();
        List<ESInfraredOriginal> list = new LinkedList<>();
        // Student student0 = new Student();
        // student0.setId(1);
        // student0.setMap(73);
        // student0.setPlace(1L);
        // student0.setPlaceName("1");
        // student0.setStartTime(new Date());
        // student0.setEndTime(new Date());
        // list.add(student0);
        for (int i = 0; i <= count; i++) {
            // findcartest student1 = new findcartest();
            // // student1.setId(2);
            // student1.setMap("74");
            // student1.setPlace(12345);
            // student1.setPlaceName("22222");
            // student1.setMapName(mapName);
            // student1.setStartTime(new Date());
            // student1.setEndTime(new Date());
            // list.add(student1);

            ESInfraredOriginal original = new ESInfraredOriginal();
            original.setTimestamp("2024-07-11 10:08:11");
            original.setGatewayNum("1000");
            original.setInfraredNum("210");
            original.setState(0);
            original.setCount(1111111);
            original.setRssi(22);
            original.setPower(0);

            list.add(original);
        }
        // operations.indexOps(ESInfraredOriginal.class).create();
        esTestMapper.saveAll(list);

        return esTestMapper.count();
    }



    @PutMapping ("/addOriginal")
    public Object addOriginal(){
        ESInfraredOriginal original = new ESInfraredOriginal();
        original.setTimestamp("2023");
        original.setGatewayNum("22");
        original.setInfraredNum("214");
        original.setState(0);
        original.setCount(11);
        original.setRssi(22);
        original.setPower(0);
        esBookRepository.save(original);
        return esBookRepository.count();
    }

    @PostMapping ("/createIndex")
    public Boolean createIndex(String indexName,Class clazz) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        if(!getIndex(createIndexRequest.index())){
            if (client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged()) {
                return elasticsearchRestTemplate.indexOps(clazz).putMapping(operations.indexOps(clazz).createMapping());
            }
        }

        return false;


    }
    @GetMapping("/getIndex")
    public Boolean getIndex(String indices) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indices);
        return client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }


    @PostMapping("/addOriginal")
    public Object addOriginal(ESInfraredOriginal original) throws IOException {
        //获取类上的注解
        // 1、 获取 User类上的注解 @ConsAnnotation
        Class<ESInfraredOriginal> clazz = ESInfraredOriginal.class;
        // 检查类上是否存在@Document注解
        if (clazz.isAnnotationPresent(Document.class)) {
            Document documentAnnotation = clazz.getAnnotation(Document.class);
            // 获取@Document注解的indexName值
            String indexName = documentAnnotation.indexName();
            if(!NullUtils.isEmpty(indexName)){
                if (getIndex(indexName)) {
                    elasticsearchRestTemplate.indexOps(ESInfraredOriginal.class).putMapping(operations.indexOps(ESInfraredOriginal.class).createMapping());
                    esTestMapper.save(original);
                }else {
                    if(createIndex(EsConst.infraredOriginal,ESInfraredOriginal.class)){
                        esTestMapper.save(original);
                    }
                }
            }
        } else {
            log.error("addOriginal:No @Document annotation found on the class");

            return false;
        }
        // return true;
        return esTestMapper.save(original);
    }

    // @GetMapping ("/test2")
    // public Object test2(){
    //     Instant start = Instant.now();
    //     SearchRequest searchRequest = new SearchRequest(EsConst.ES_INDEX);
    //     SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    //     sourceBuilder.query();
    //     Instant end = Instant.now();
    //     Duration elapsedTime = Duration.between(start, end);
    //     System.out.println("代码执行时间：" + elapsedTime.toMillis() + "毫秒");
    //     Map<String,Object> map = new HashMap<>();
    //     map.put("count", esTestMapper.count());
    //     map.put("", );
    //
    //     return map;
    // }

    @GetMapping ("/get")
    public Object get() throws Exception {
        Map<String,Object> map = new HashMap<>();
        List<Student> students = new ArrayList<>();

        SearchRequest searchRequest = new SearchRequest(EsConst.ES_INDEX);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termQuery("map", "74"));
        // sourceBuilder.from(0);
        // sourceBuilder.size(20);
        // AggregationBuilder aggregation = AggregationBuilders.terms("map").field("map");
        // sourceBuilder.aggregation(aggregation);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse= client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println("searchResponse.getHits() = " + JSON.toJSONString(searchResponse.getHits()));
        System.out.println("====================");
        for (org.elasticsearch.search.SearchHit documentFields : searchResponse.getHits().getHits()){
            System.out.println("documentFields = " + documentFields.getSourceAsMap());
            BeanHandler<Student> beanHandler = new BeanHandler<>(Student.class);
            Student student = beanHandler.handle(documentFields.getSourceAsMap());
            students.add(student);
        }

        long count = esTestMapper.count();

        map.put("data",students);
        map.put("count",count);
        return map;
    }



    @GetMapping("/test1")
    public Object test() throws IOException {
            Instant start = Instant.now();
            // 1. 创建查询请求对象
            SearchRequest query = new SearchRequest(EsConst.infraredOriginal);
            // 2. 构建搜索条件
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            // 构建聚合，按照map字段进行分组
            TermsAggregationBuilder mapAggregation = AggregationBuilders.terms("gatewayNum").field("gatewayNum");
            // 添加子聚合，统计每个map下的文档数量
            ValueCountAggregationBuilder countAggregation = AggregationBuilders.count("count").field("gatewayNum");
            // 添加子聚合，获取每个map下的name和mapname
            TermsAggregationBuilder nameAggregation = AggregationBuilders.terms("infraredNum").field("infraredNum").size(10);
            TermsAggregationBuilder mapnameAggregation = AggregationBuilders.terms("gatewayNum").field("gatewayNum").size(10);
            // 将子聚合添加到map聚合中
            mapAggregation.subAggregation(countAggregation);
            mapAggregation.subAggregation(nameAggregation);
            mapAggregation.subAggregation(mapnameAggregation);
            // 将map聚合添加到搜索条件中
            sourceBuilder.aggregation(mapAggregation);
            query.source(sourceBuilder);

            // 执行搜索请求
            SearchResponse search = client.search(query, RequestOptions.DEFAULT);
            Instant end = Instant.now();
            Duration elapsedTime = Duration.between(start, end);
            System.out.println("search代码执行时间：" + elapsedTime.toMillis() + "毫秒");
            Aggregations aggregations = search.getAggregations();
            if (aggregations != null) {
                Terms mapTerms = aggregations.get("gatewayNum");
                List<Student> result = new ArrayList<>();
                for (Terms.Bucket mapBucket : mapTerms.getBuckets()) {
                    Student student = new Student();
                    student.setName(mapBucket.getKeyAsString());
                    student.setCount(mapBucket.getDocCount());

                    Terms nameTerms = mapBucket.getAggregations().get("gatewayNum");
                    if (nameTerms.getBuckets().size() > 0) {
                        student.setName(nameTerms.getBuckets().get(0).getKeyAsString());
                    }

                    Terms mapnameTerms = mapBucket.getAggregations().get("gatewayNum");
                    if (mapnameTerms.getBuckets().size() > 0) {
                        student.setMapName(mapnameTerms.getBuckets().get(0).getKeyAsString());
                    }

                    result.add(student);
                }
                Instant end1 = Instant.now();
                Duration elapsedTime1 = Duration.between(start, end1);
                System.out.println("search代码执行时间：" + elapsedTime1.toMillis() + "毫秒");
                return result;

            } else {
                return search;
            }
    }

    @GetMapping("/map-stats")
    public Object  getMapStats() throws IOException {
        // 1.创建查询请求对象
        SearchRequest query = new SearchRequest(EsConst.findcartest);
        // 2.构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 构建聚合，聚合之间可以进行嵌套聚合，如果要先分组然后sum或者avg时，需要嵌套
        // 分组。terms分组名称、field分组字段；默认分组后的结果为count
        TermsAggregationBuilder builder = AggregationBuilders.terms("map").field("map");
        TermsAggregationBuilder mapname = AggregationBuilders.terms("mapname").field("mapname");
        // 其他聚合，比如:AVG/SumAggregationBuilder等
        ValueCountAggregationBuilder count=  AggregationBuilders.count("count_map").field("map");
        builder.subAggregation(count);
        sourceBuilder.aggregation(builder);
        sourceBuilder.aggregation(mapname);
        query.source(sourceBuilder);
        SearchResponse search = client.search(query, RequestOptions.DEFAULT);
        Aggregations aggregations = search.getAggregations();
        if (aggregations != null) {
            Terms terms = (Terms) aggregations.asMap().get("map");
            List<Student> productAggList = new ArrayList<>();
            // 遍历取出聚合字段列的值，与对应的数量
            for (Terms.Bucket bucket : terms.getBuckets()) {
                Student productAgg = new Student();
                productAgg.setMap(bucket.getKeyAsString());
                productAgg.setCount(bucket.getDocCount());
                // 默认value就是count
                productAgg.setValue(bucket.getDocCount());
                // 解析嵌套聚合
                Aggregations sumAggregations = bucket.getAggregations();
                if (sumAggregations != null) {
                    // ParsedXxx的选择需要根据聚合计算的方式来定
                    ParsedValueCount parsedValueCount = sumAggregations.get("count_map");
                    productAgg.setValue(parsedValueCount.getValue());
                }
                productAggList.add(productAgg);
            }
            return productAggList;
        } else {
            return search;
        }
    }


    @GetMapping ("batchInsert")
    public String batchInsert(Integer num1,String map,String mapName) {
        int num = num1;
        CountDownLatch latch = new CountDownLatch(1);
        List<findcartest> userList = new ArrayList<>();
        new Thread(() -> {
            for (int i = 0; i < 1000000; i++) {
                findcartest student1 = new findcartest();
                student1.setMap(map);
                student1.setMapName(mapName);
                student1.setPlace(11);
                student1.setPlaceName("11");
                student1.setStartTime(new Date());
                student1.setEndTime(new Date());
                userList.add(student1);
            }
            latch.countDown();
        }).start();
        try {
            latch.await();
        } catch (InterruptedException ignored) {
        }
        //2000条为一批，插入1000万条
        List<List<findcartest>> partition = Lists.partition(userList, num);
        partition.forEach(students -> {
            // esTestMapper.saveAll(students).iterator();
        });
        return "1";
    }


    //创建索引库
    @GetMapping("/testCreateIndex")
    public void testCreateIndex() throws IOException {
        ESInfraredOriginal original = new ESInfraredOriginal();
        CreateIndexRequest request = new CreateIndexRequest(EsConst.ES_INDEX);
        request.mapping(JSON.toJSONString(ESInfraredOriginal.class),XContentType.JSON);
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());// 查看是否创建成功
        System.out.println(response);// 查看返回对象
        client.close();
    }

    @GetMapping("/testAddDocument")
    public void testAddDocument(ESInfraredOriginal ESInfraredOriginal) throws IOException {
    // public void testAddDocument(String  timestamp,String gateway,String infrared,Integer count) throws IOException {
        // 创建一个User对象
        // ESInfraredOriginal liuyou = new ESInfraredOriginal();
        // liuyou.setTimestamp(timestamp);
        // liuyou.setGatewayNum(gateway);
        // liuyou.setInfraredNum(infrared);
        // liuyou.setState(0);
        // liuyou.setCount(count);
        // liuyou.setRssi(-10);
        // liuyou.setPower(-11);
        // 创建请求
        IndexRequest request = new IndexRequest("infrared-original");
        // 制定规则 PUT /infrared-original/_doc/1
        request.timeout(TimeValue.timeValueMillis(1000));// request.timeout("1s")
        // 将我们的数据放入请求中
        request.source(JSON.toJSONString(ESInfraredOriginal), XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        // 客户端发送请求，获取响应的结果
        System.out.println(response.status());// 获取建立索引的状态信息 CREATED
        System.out.println(response);// 查看返回内容 IndexResponse[index=infrared-original,type=_doc,id=1,version=1,result=created,seqNo=0,primaryTerm=1,shards={"total":2,"successful":1,"failed":0}]
    }



    @GetMapping("/map-stats1")
    public Object  getMapStats1() throws IOException {
        // 1.创建查询请求对象
        SearchRequest query = new SearchRequest("infrared-original");
        // 2.构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 构建聚合，聚合之间可以进行嵌套聚合，如果要先分组然后sum或者avg时，需要嵌套
        // 分组。terms分组名称、field分组字段；默认分组后的结果为count
        TermsAggregationBuilder builder = AggregationBuilders.terms("infraredNum").field("infraredNum");
        TermsAggregationBuilder mapname = AggregationBuilders.terms("gatewayNum").field("gatewayNum");
        // 其他聚合，比如:AVG/SumAggregationBuilder等
        ValueCountAggregationBuilder count=  AggregationBuilders.count("count_map").field("infraredNum");
        builder.subAggregation(count);
        sourceBuilder.aggregation(builder);
        sourceBuilder.aggregation(mapname);
        query.source(sourceBuilder);
        SearchResponse search = client.search(query, RequestOptions.DEFAULT);
        Aggregations aggregations = search.getAggregations();
        if (aggregations != null) {
            Terms terms = (Terms) aggregations.asMap().get("infraredNum");
            List<Student> productAggList = new ArrayList<>();
            // 遍历取出聚合字段列的值，与对应的数量
            for (Terms.Bucket bucket : terms.getBuckets()) {
                Student productAgg = new Student();
                productAgg.setMap(bucket.getKeyAsString());
                productAgg.setCount(bucket.getDocCount());
                // 默认value就是count
                productAgg.setValue(bucket.getDocCount());
                // 解析嵌套聚合
                Aggregations sumAggregations = bucket.getAggregations();
                if (sumAggregations != null) {
                    // ParsedXxx的选择需要根据聚合计算的方式来定
                    ParsedValueCount parsedValueCount = sumAggregations.get("count_map");
                    productAgg.setValue(parsedValueCount.getValue());
                }
                productAggList.add(productAgg);
            }
            return productAggList;
        } else {
            return search;
        }
    }



    @GetMapping("searchInfraredOriginal")
    public List<ESInfraredOriginal> searchInfraredOriginal(String gatewayNum, String infraredNum, Integer state, String startTime, String endTime) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (gatewayNum != null) {
            boolQuery.must(QueryBuilders.termQuery("gatewayNum", gatewayNum));
        }
        if (infraredNum != null) {
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
                .collect(Collectors.groupingBy(com.tgy.rtls.data.entity.es.ESInfraredOriginal::getCount,Collectors.summingInt(it -> 1)));

        // Convert SearchHits to a List<ESInfraredOriginal>
        List<ESInfraredOriginal> list1 = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        // Set appearCount for the last data object
        if (!list1.isEmpty()) {
            ESInfraredOriginal lastESInfraredOriginal = list1.get(list1.size() - 1);
            Integer count = Math.toIntExact(countOccurrences.getOrDefault(lastESInfraredOriginal.getCount(), 0));
            lastESInfraredOriginal.setAppearCount(count);
        }
        // Calculate time difference for each data object
        list1.stream()
                .peek(currentData -> {
                    Date currentTime = parseTimestamp(currentData.getTimestamp());

                    int index = list1.indexOf(currentData);
                    if (index > 0) {
                        ESInfraredOriginal previousData = list1.get(index - 1);
                        Date previousTime = parseTimestamp(previousData.getTimestamp());
                        int timeDifference = (int) ((Objects.requireNonNull(currentTime).getTime() - Objects.requireNonNull(previousTime).getTime()) / 1000);
                        currentData.setTimePoor(timeDifference);
                    } else {
                        currentData.setTimePoor(0);
                    }

                })
                .collect(Collectors.toList());

        // SearchHits<ESInfraredOriginal> searchHits = elasticsearchRestTemplate.search(searchQuery, ESInfraredOriginal.class);
        // // Count occurrences of each count value
        // Map<Integer, Integer> countOccurrences = new HashMap<>();
        // for (SearchHit<ESInfraredOriginal> hit : searchHits) {
        //     list.add(hit.getContent());
        //     Integer count = hit.getContent().getCount();
        //     countOccurrences.put(count, countOccurrences.getOrDefault(count, 0) + 1);
        // }
        //
        // // Set appearCount for the last data object
        // if (!searchHits.isEmpty()) {
        //     int lastDataIndex = searchHits.getSearchHits().size() - 1;
        //     int lastDataCount = searchHits.getSearchHit(lastDataIndex).getContent().getCount();
        //     Integer appearCount = countOccurrences.getOrDefault(lastDataCount, 0);
        //     searchHits.getSearchHit(lastDataIndex).getContent().setAppearCount(appearCount);
        // }

        // for (int i = 0; i < list.size(); i++) {
        //     ESInfraredOriginal currentData = list.get(i);
        //     Date currentTime = parseTimestamp(currentData.getTimestamp());
        //
        //     if (i > 0) {
        //         ESInfraredOriginal previousData = list.get(i - 1);
        //         Date previousTime = parseTimestamp(previousData.getTimestamp());
        //         int timeDifference = (int) ((currentTime.getTime() - previousTime.getTime())  / 1000);
        //         currentData.setTimePoor(timeDifference);
        //     } else {
        //         // If it's the first data object, set timePoor to 0 or any default value
        //         currentData.setTimePoor(0);
        //     }
        // }

        return list1;
    }


    // Helper method to parse timestamp string to Date object
    private Date parseTimestamp(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return sdf.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
