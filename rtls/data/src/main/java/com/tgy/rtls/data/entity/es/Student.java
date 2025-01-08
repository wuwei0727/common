package com.tgy.rtls.data.entity.es;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity
 * @Author: wuwei
 * @CreateTime: 2023-10-16 18:00
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
//indexName名字如果是字母那么必须是小写字母
@Document (indexName = "findcar")
public class Student implements Serializable {
    @Id
    private String id;

    @ApiModelProperty (value="地图Id")
    @Field(type = FieldType.Integer)
    private String map;

    @ApiModelProperty(value="车位Id")
    @Field(type = FieldType.Long)
    private Long place;

    @ApiModelProperty(value="车位名称")
    @Field(value = "placename",type = FieldType.Text,analyzer = "ik_max_word")
    private String placeName;
    @Field(value = "mapName",type = FieldType.Text,analyzer = "ik_max_word")
    private String mapName;

    @ApiModelProperty(value="开始时间")
    @Field(value = "starttime",type = FieldType.Date, format = DateFormat.custom,pattern = "uuuu-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty(value="结束时间")
    @Field(value = "endtime",store = true, type = FieldType.Date,format = DateFormat.custom,pattern = "uuuu-MM-dd HH:mm:ss")
    private Date endTime;

    private String name;
    private long count;
    private Object value;
}
