package com.tgy.rtls.web.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.search.SearchHit;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
*@Author: wuwei
*@CreateTime: 2024/6/28 11:29
*/
@Component
public class ElasticsearchUtil {

    private final RestHighLevelClient client;
    private final ObjectMapper objectMapper;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    public ElasticsearchUtil(RestHighLevelClient client,ObjectMapper objectMapper,ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.client = client;
        this.objectMapper=objectMapper;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
    }

    public Boolean createIndex(String indexName) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        if (getIndex(createIndexRequest.index())) {
            // 删除已有的索引（如果存在）
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
            client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        }
        if (client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged()) {
            Set<Class<?>> entityClass = findClassesWithIndexName(indexName);
            if (entityClass.isEmpty()) {
                // 如果找不到对应的实体类，返回 false 或者抛出异常
                return false;
            }
            return elasticsearchRestTemplate.indexOps(entityClass.iterator().next())
                    .putMapping(elasticsearchRestTemplate.indexOps(entityClass.iterator().next()).createMapping());
        }
        return false;
    }

    public  Boolean getIndex(String indices) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indices);
        return client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    public Set<Class<?>> findClassesWithIndexName(String indexName) {
        Set<Class<?>> matchedClasses = new HashSet<>();

        // 使用Reflections库扫描带有@Document注解的类
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages("com.tgy.rtls.data.entity.es")
                .addScanners(Scanners.TypesAnnotated));
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(Document.class);

        // 遍历找到的类并检查注解的indexName属性值
        for (Class<?> clazz : annotatedClasses) {
            Document documentAnnotation = clazz.getAnnotation(Document.class);
            if (documentAnnotation != null && indexName.equals(documentAnnotation.indexName())) {
                matchedClasses.add(clazz);
            }
        }

        return matchedClasses;
    }
    public <T> List<T> convertSearchResponse(SearchRequest searchRequest, Class<T> entityClass) throws IOException{
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        List<T> results = new ArrayList<>();

        for (SearchHit hit : searchResponse.getHits().getHits()) {
            T entity = objectMapper.readValue(hit.getSourceAsString(), entityClass);
            try {

                Field idField = entityClass.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(entity, Long.valueOf(hit.getId()));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            results.add(entity);
        }
        return results;
    }

}