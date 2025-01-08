package com.tgy.rtls.web.controller.ES;

import com.tgy.rtls.web.util.DatabaseUtil;
import com.tgy.rtls.web.util.ElasticsearchUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.ES
 * @Author: wuwei
 * @CreateTime: 2024-06-28 16:00
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/data-import")
@Slf4j
public class MysqlToEsController {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Value("${esHost}")
    private static String esHost;
    @Value("${esPort}")
    private static int esPort;
    @Resource
    private ElasticsearchUtil elasticsearchUtil;
    @Resource
    private RestHighLevelClient client;
    ;
    @Resource
    private ElasticsearchOperations operations;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    public static RestHighLevelClient createElasticsearchClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(esHost, esPort, "http"));
        return new RestHighLevelClient(builder);
    }

    @PutMapping("mysqlToElasticsearch")
//    public boolean mysqlToElasticsearch(@RequestParam String indexName,@RequestParam String sql) {
    public boolean mysqlToElasticsearch(@RequestBody Map<String, String> params) {
        boolean allSuccess = true;  // 添加一个标志来跟踪所有操作是否成功

        try (Connection connection = DatabaseUtil.getConnection()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String indexName = entry.getKey();
                String sql = entry.getValue();
                boolean currentSuccess = false;  // 当前索引处理是否成功

                // 创建索引并设置IK分词器
//            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
//            Settings settings = Settings.builder()
//                    .put("index.analysis.analyzer.default.tokenizer", "ik_max_word")
//                    .build();
//            createIndexRequest.settings(settings);

                if (elasticsearchUtil.createIndex(indexName)) {
                    PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery();

                    BulkRequest bulkRequest = new BulkRequest();
                    int documentCount = 0;  // 记录处理的文档数

                    while (resultSet.next()) {
                        Map<String, Object> jsonMap = new HashMap<>();
                        ResultSetMetaData metaData = resultSet.getMetaData();
                        int columnCount = metaData.getColumnCount();

                        String id = resultSet.getString("id");

                        // 检查文档是否已经存在
                        GetRequest getRequest = new GetRequest(indexName, id);
                        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
                        if (!exists) {
                            for (int i = 1; i <= columnCount; i++) {
                                String columnName = metaData.getColumnName(i);
                                Object columnValue = resultSet.getObject(i);

                                // 处理Timestamp类型
                                if (columnValue instanceof Timestamp) {
                                    columnValue = dateFormat.format((Timestamp) resultSet.getObject(i));
                                } else if (columnValue != null) {
                                    // 将非Timestamp类型转换为字符串
                                    columnValue = columnValue.toString();
                                }

                                jsonMap.put(columnName, columnValue);
                            }
                        }
                        IndexRequest indexRequest = new IndexRequest(indexName).id(id).source(jsonMap);
                        bulkRequest.add(indexRequest);
                        documentCount++;
                    }

                    if (bulkRequest.numberOfActions() > 0) {
                        Instant start = Instant.now();
                        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                        Instant end = Instant.now();
                        Duration elapsedTime = Duration.between(start, end);
                        log.info(indexName + " - Bulk operation completed in " + elapsedTime.toMillis() + "ms");
                        log.info(indexName + " - Processed " + documentCount + "");
                        if (bulkResponse.hasFailures()) {
                            log.error(indexName + " - Bulk operation has failures: " + bulkResponse.buildFailureMessage());
                            currentSuccess = false;
                        } else {
                            log.info(indexName + " - Bulk operation completed successfully");
                            currentSuccess = true;
                        }
                    }else {
                        log.info(indexName + " - No documents to process");
                        currentSuccess = true;
                    }
                }
                // 如果当前索引处理失败，设置总体状态为失败
                if (!currentSuccess) {
                    allSuccess = false;
                }
            }

        } catch (Exception e) {
            log.error("Error processing indexes: ", e);
            e.printStackTrace();
        }
        return allSuccess;
    }


}
