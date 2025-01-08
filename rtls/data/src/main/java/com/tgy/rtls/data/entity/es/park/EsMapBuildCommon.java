package com.tgy.rtls.data.entity.es.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

/**
 * @author wuwei
 * @date 2024/2/23 - 9:50
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "map_build")
public class EsMapBuildCommon implements Serializable {
    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Integer)
    private Integer map;
    @Field(type = FieldType.Keyword)
    private String floor;
    @Field(type = FieldType.Keyword,index = false)
    private String fid;
    @Field(type = FieldType.Keyword,index = false)
    private String x;
    @Field(type = FieldType.Keyword,index = false)
    private String y;
    @Field(type = FieldType.Keyword,value="object_type",index = false)
    private String objectType;
    @Field(type = FieldType.Keyword,value = "icon_type",index = false)
    private String iconType;
}
