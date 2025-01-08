package com.tgy.rtls.data.entity.es.park;

import com.fasterxml.jackson.annotation.JsonFormat;
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

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.es
 * @Author: wuwei
 * @CreateTime: 2024-06-27 19:40
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "parking_company")
public class EsCompany implements Serializable {
    @Id
    private Long id;
    //ik_smart ik_max_word
    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Keyword)
    private String user;
    @Field(type = FieldType.Keyword,index = false)
    private String pwd;
    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Integer,index = false)
    private String role;
    @Field(type = FieldType.Keyword)
    private String floor;//楼层
    @Field(type = FieldType.Keyword,index = false)
    private String x;
    @Field(type = FieldType.Keyword,index = false)
    private String y;

    @Field(type = FieldType.Keyword)
    private String fid;
    @Field(type = FieldType.Keyword)
    private String map;

    @Field(type = FieldType.Integer,index = false)
    private String instanceid;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Field(value = "addTime",type = FieldType.Date, format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime addTime;
    private static final long serialVersionUID = 1L;
}
