package com.tgy.rtls.data.entity.es;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.es
 * @Author: wuwei
 * @CreateTime: 2024-11-01 16:39
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "mag")
public class ESMag {
    @Id
    private String id;
    @Field(type = FieldType.Keyword)
    private String num;//定位卡编号

    @Field(type = FieldType.Keyword)
    private String time;   //创建时间

    @Field(type = FieldType.Text)
    private String x_fix;
    @Field(type = FieldType.Text)
    private String x;
    @Field(type = FieldType.Text)
    private String y_fix;
    @Field(type = FieldType.Text)
    private String y;
    @Field(type = FieldType.Text)
    private String z_fix;
    @Field(type = FieldType.Text)
    private String z;
    @Field(type = FieldType.Text)
    private String x_diff;
    @Field(type = FieldType.Text)
    private String occupy_x;
    @Field(type = FieldType.Text)
    private String y_diff;
    @Field(type = FieldType.Text)
    private String occupy_y;
    @Field(type = FieldType.Text)
    private String z_diff;
    @Field(type = FieldType.Text)
    private String occupy_z;
    @Field(type = FieldType.Text)
    private String empty_x;
    @Field(type = FieldType.Text)
    private String empty_y;
    @Field(type = FieldType.Text)
    private String empty_z;
    @Field(type = FieldType.Integer)
    private Integer state;
}
