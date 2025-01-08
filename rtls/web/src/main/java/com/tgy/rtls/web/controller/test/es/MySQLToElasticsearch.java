package com.tgy.rtls.web.controller.test.es;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgy.rtls.web.util.DatabaseUtil;
import com.tgy.rtls.web.util.ElasticsearchUtil;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class MySQLToElasticsearch {
    private static final Logger log = LoggerFactory.getLogger(MySQLToElasticsearch.class);

    private static final String ES_HOST = "192.168.1.131";
    private static final int ES_PORT = 9200;
    private static final String INDEX_NAME = "parking_company";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static void main(String[] args) {

        RestHighLevelClient client = createElasticsearchClient();
        ObjectMapper objectMapper = new ObjectMapper();
//        ElasticsearchUtil elasticsearchUtil = new ElasticsearchUtil(client, objectMapper, new ElasticsearchRestTemplate(client));
        ElasticsearchUtil elasticsearchUtil = null;

        Map<String, String> indexSqlMap = new HashMap<>();
        indexSqlMap.put("parking_company", "SELECT * FROM parking_company");
        indexSqlMap.put("shangjia", "SELECT * FROM shangjia");
        indexSqlMap.put("parking_place", "SELECT * FROM parking_place");
        indexSqlMap.put("parking_elevator_binding", "SELECT * FROM parking_elevator_binding");
        indexSqlMap.put("parking_exit", "SELECT * FROM parking_exit");
        indexSqlMap.put("map_build", "SELECT * FROM map_build");
        indexSqlMap.put("map_wc", "SELECT * FROM map_wc");
        try (Connection connection = DatabaseUtil.getConnection()) {
            for (Map.Entry<String, String> entry : indexSqlMap.entrySet()) {
                String indexName = entry.getKey();
                String sql = entry.getValue();
                // 创建索引并设置IK分词器
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
                Settings settings = Settings.builder()
                        .put("index.analysis.analyzer.default.tokenizer", "ik_max_word")
                        .build();

                createIndexRequest.settings(settings);
                if (elasticsearchUtil.createIndex(indexName)) {
                    PreparedStatement statement = connection.prepareStatement(sql);
                    ResultSet resultSet = statement.executeQuery();

                    BulkRequest bulkRequest = new BulkRequest();

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
                    }

                    if (bulkRequest.numberOfActions() > 0) {
                        Instant start = Instant.now();
                        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                        Instant end = Instant.now();
                        Duration elapsedTime = Duration.between(start, end);
                        log.error("bulkResponse代码执行时间：" + elapsedTime.toMillis() + "毫秒");
                        if (bulkResponse.hasFailures()) {
                            log.error("Bulk operation has failures: " + bulkResponse.buildFailureMessage());
                        } else {
                            log.error("Bulk operation completed successfully.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createIndexWithIKAnalyzer() {
        try (RestHighLevelClient client = createElasticsearchClient()) {
            CreateIndexRequest request = new CreateIndexRequest(INDEX_NAME);

            // 设置索引的设置，包括分片和副本数
            request.settings(Settings.builder()
                    .put("index.number_of_shards", 1)
                    .put("index.number_of_replicas", 1)
                    .put("analysis.analyzer.default.type", "ik_max_word") // 设置默认分析器为ik_max_word
            );

            // 发送创建索引的请求
            client.indices().create(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static RestHighLevelClient createElasticsearchClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(ES_HOST, ES_PORT, "http"));
        return new RestHighLevelClient(builder);
    }


}