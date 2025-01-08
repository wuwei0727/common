package com.tgy.rtls.data.entity.es.park;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "parking_place")
public class EsParkingPlace implements Serializable {
    @Id
    private Long id;
    @Field(type = FieldType.Text)
    private String name;
    @Field(type = FieldType.Keyword,index = false)
    private String x;
    @Field(type = FieldType.Keyword,index = false)
    private String y;
    @Field(type = FieldType.Keyword,index = false)
    private String z;
    @Field(type = FieldType.Keyword)
    private Integer map;
    @Field(type = FieldType.Keyword,value = "config_way")
    private Integer configWay=1;
    @Field(type = FieldType.Keyword)
    private Integer company;
    @Field(type = FieldType.Keyword,value = "elevator_id")
    private Integer elevatorId;
    @Field(type = FieldType.Keyword)
    private String floor;
    @Field(type = FieldType.Keyword)
    private Integer state=3;
    @Field(type = FieldType.Keyword)
    private String license;
    @Field(type = FieldType.Keyword,index = false)
    private String instanceid;
    @Field(type = FieldType.Keyword,index = false)
    private String fid;
    @Field(type = FieldType.Keyword)
    private String carbittype;
    @Field(type = FieldType.Keyword)
    private Integer type;
    @Field(type = FieldType.Keyword)
    private Integer charge=0;
    @Field(value = "addTime",type = FieldType.Date, format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addTime;
    @Field(value = "addTime",type = FieldType.Date, format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}


