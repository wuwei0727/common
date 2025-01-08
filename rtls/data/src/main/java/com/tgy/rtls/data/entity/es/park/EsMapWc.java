package com.tgy.rtls.data.entity.es.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @author wuwei
 * @date 2024/2/23 - 9:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "map_wc")
public class EsMapWc {
    @Id
    private Integer id;

    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Keyword)
    private Integer map;
    @Field(type = FieldType.Keyword)
    private String floor;
    @Field(type = FieldType.Keyword,index = false)
    private String fid;
    @Field(type = FieldType.Keyword,index = false)
    private String x;
    @Field(type = FieldType.Keyword,index = false)
    private String y;
    @Field(type = FieldType.Keyword,index = false,value = "object_type")
    private String objectType;
    @Field(type = FieldType.Keyword,index = false,value = "icon_type")
    private String iconType;
}
