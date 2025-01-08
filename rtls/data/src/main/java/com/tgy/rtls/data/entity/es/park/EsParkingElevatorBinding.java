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
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.entity.park
*@Author: wuwei
*@CreateTime: 2023-09-12 09:43
*@Description: TODO
*@Version: 1.0
*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "parking_elevator_binding")
public class EsParkingElevatorBinding implements Serializable {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword,index = false)
    private String x;
    @Field(type = FieldType.Keyword,index = false)
    private String y;
    @Field(type = FieldType.Keyword)
    private Integer map;

    @Field(type = FieldType.Keyword)
    private String building;
    @Field(type = FieldType.Keyword,index = false)
    private String fid;
    @Field(type = FieldType.Integer)
    private Integer floor;

    @Field(type = FieldType.Keyword,index = false,value="object_type")
    private String objectType;
    @Field(type = FieldType.Keyword,index = false,value="icon_type")
    private String iconType;
    private static final long serialVersionUID = 1L;
}
