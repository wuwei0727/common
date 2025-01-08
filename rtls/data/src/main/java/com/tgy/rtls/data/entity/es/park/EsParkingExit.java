package com.tgy.rtls.data.entity.es.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "parking_exit")
public class EsParkingExit implements Serializable {
    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Keyword,index = false)
    private String x;
    @Field(type = FieldType.Keyword,index = false)
    private String y;
    @Field(type = FieldType.Keyword,index = false)
    private String fid;
    @Field(type = FieldType.Keyword)
    private Integer map;
    @Field(type = FieldType.Keyword,value = "access_status")
    private Integer accessStatus;
    @Field(type = FieldType.Keyword,value = "road_name")
    private String roadName;
    @Field(type = FieldType.Keyword)
    private String floor;
    @Field(type = FieldType.Keyword)
    private Integer type=0;
    @Field(type = FieldType.Keyword,index = false)
    private String z;
    @Field(type = FieldType.Keyword,index = false)
    private String doorx;
    @Field(type = FieldType.Keyword,index = false)
    private String doory;
}