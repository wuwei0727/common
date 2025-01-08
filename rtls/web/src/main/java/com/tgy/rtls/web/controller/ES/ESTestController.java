package com.tgy.rtls.web.controller.ES;

import com.tgy.rtls.data.entity.es.ESInfraredOriginal;
import com.tgy.rtls.data.entity.es.Student;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.controller.ES
 * @Author: wuwei
 * @CreateTime: 2024-06-27 13:52
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping(value = "/es")
@CrossOrigin
@Slf4j
public class ESTestController {
    @Resource
    private RestHighLevelClient client;
    @Resource
    private ElasticsearchOperations operations;;
    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Resource
    private SimpleElasticsearchMappingContext mappingContext;

//    @Bean
//    public SimpleElasticsearchMappingContext mappingContext() {
//        return new SimpleElasticsearchMappingContext();
//    }
//
    @PostMapping("/createIndex1")
    public Boolean createIndex1(@RequestParam String indexName) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
        if(!getIndex1(createIndexRequest.index())){
            if (client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged()) {
                Set<Class<?>> entityClass = findClassesWithIndexName("com.tgy.rtls.data.entity.es", indexName);
                if (entityClass.isEmpty()) {
                    // 如果找不到对应的实体类，返回 false 或者抛出异常
                    return false;
                }
                return elasticsearchRestTemplate.indexOps(entityClass.iterator().next()).putMapping(operations.indexOps(ESInfraredOriginal.class).createMapping());
            }
        }

        return false;
    }


    @PostMapping("/aaa")
    public Boolean aa(@RequestParam String indexName,String id) throws IOException {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
//        BulkRequest request = new BulkRequest();
//        request.add(new IndexRequest(indexName).id(id+1).source(map, XContentType.JSON));

            if (client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged()) {
                Set<Class<?>> entityClass = findClassesWithIndexName("com.tgy.rtls.data.entity.es", indexName);
                if (entityClass.isEmpty()) {
                    // 如果找不到对应的实体类，返回 false 或者抛出异常
                    return false;
                }

                return elasticsearchRestTemplate.indexOps(entityClass.iterator().next()).putMapping(operations.indexOps(ESInfraredOriginal.class).createMapping());
            }


        return false;
    }
    @PostMapping("/aaa1")
    public void testCreateIndexWithoutIndex() throws IOException {
//        CreateIndexRequest createIndexRequest = new CreateIndexRequest("findcar");
//        client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
//        // 根据索引实体，获取mapping字段
//        org.springframework.data.elasticsearch.core.document.Document mapping = elasticsearchRestTemplate.indexOps(Student.class).createMapping();
//        // 创建索引mapping
//        elasticsearchRestTemplate.indexOps(Student.class).putMapping(mapping);
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("findcar");

        if (client.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged()) {
             elasticsearchRestTemplate.indexOps(Student.class).putMapping(operations.indexOps(Student.class).createMapping());
        }
    }

    @GetMapping("/getIndex1")
    public Boolean getIndex1(String indices) throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indices);
        return client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
    }

    @GetMapping("/delIndex1")
    public Boolean delIndex1(String indexName) throws IOException {
        // 先检查索引是否存在
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        if (!exists) {
            // 如果索引不存在，返回false
            return false;
        }

        // 删除索引
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
        client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        return true;
    }


    public static Set<Class<?>> findClassesWithIndexName(String packageName,String indexName) throws IOException {
        Set<Class<?>> matchedClasses = new HashSet<>();

        // 使用Reflections库扫描带有@Document注解的类
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackages(packageName)
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



//    requestIndex.settings(Settings.builder().put("index.number_of_shards", 3)
//				.put("index.number_of_replicas", 2)
//				.put("index.refresh_interval", "-1"));
    public static long importData(String sql){
        String url = "jdbc:mysql://192.168.1.95:3306/park1?user=root&password=tuguiyao";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        long allStart = System.currentTimeMillis();
        long count =0;

        Connection con = null;
        PreparedStatement ps = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = DriverManager.getConnection(url);

            ps = con.prepareStatement(sql,ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            ps.setFetchSize(Integer.MIN_VALUE);

            ps.setFetchDirection(ResultSet.FETCH_REVERSE);

            rs = ps.executeQuery();


            while (rs.next()) {
                System.out.println("rs = " + rs.getString("name"));
                //此处处理业务逻辑
                count++;
                if(count%600000==0){
                    System.out.println(" 写入到第  "+(count/600000)+" 个文件中！");
                    long end = System.currentTimeMillis();
                }

            }
            System.out.println("取回数据量为  "+count+" 行！");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(rs!=null){
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if(ps!=null){
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if(con!=null){
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return count;

    }

    public static void main(String[] args) throws InterruptedException, IOException {

        String sql = "select * from parking_place";
        importData(sql);
        Set<Class<?>> classes = findClassesWithIndexName("com.tgy.rtls.data.entity.es", "findcar");
        for (Class<?> clazz : classes) {
            System.out.println("Found class: " + clazz.getClasses());
        }

    }
}
