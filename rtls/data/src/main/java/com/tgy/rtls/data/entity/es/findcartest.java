package com.tgy.rtls.data.entity.es;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.es
 * @Author: wuwei
 * @CreateTime: 2023-10-18 18:38
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document (indexName = "findcarq")
public class findcartest {
    private static final long serialVersionUID = -3843548915035470817L;
    @Id
    private String id;
    @Field(type = FieldType.Keyword)
    private String map;
    @Field(type = FieldType.Keyword)
    private String mapName;
    private Integer place;
    @Field(type = FieldType.Keyword)
    private String placeName;
    @Field(type = FieldType.Date,format = DateFormat.custom,pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSX")
    private Date startTime;
    @Field(type = FieldType.Date,format = DateFormat.custom,pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSX")
    private Date endTime;
    private String name;
    private long count;
    private Object value;
}
