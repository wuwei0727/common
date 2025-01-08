// package com.tgy.rtls.data.config;
//
// import org.elasticsearch.client.RestHighLevelClient;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.data.elasticsearch.client.ClientConfiguration;
// import org.springframework.data.elasticsearch.client.RestClients;
// import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
//
// /**
//  * @BelongsProject: rtls
//  * @BelongsPackage: com.tgy.rtls.data.config
//  * @Author: wuwei
//  * @CreateTime: 2023-10-16 11:15
//  * @Description: TODO
//  * @Version: 1.0
//  * ElasticSearch 客户端配置
//  */
// @Configuration
// public class ElasticSearchClientConfig extends AbstractElasticsearchConfiguration {
//     @Override
//     @Bean
//     public RestHighLevelClient elasticsearchClient() {
//         final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
//                 .connectedTo("localhost:9200")
//                 .build();
//         return RestClients.create(clientConfiguration).rest();
//     }
// }
