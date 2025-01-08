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
@Document(indexName = "shangjia")
public class EsShangJia implements Serializable {
  @Id
  private Long id;
  @Field(type = FieldType.Text)
  private String name;
  @Field(type = FieldType.Keyword)
  private Integer map;
  @Field(type = FieldType.Keyword)
  private String owner;
  @Field(type = FieldType.Keyword)
  private String phone;
  @Field(type = FieldType.Keyword)
  private String content;
  @Field(type = FieldType.Keyword)
  private String address;
  @Field(type = FieldType.Keyword,index = false)
  private String floor;
  @Field(type = FieldType.Keyword)
  private Integer instanceid;
  @Field(type = FieldType.Keyword)
  private Integer type;
  @Field(type = FieldType.Keyword,index = false)
  private String x;
  @Field(type = FieldType.Keyword,index = false)
  private String y;
  @Field(type = FieldType.Keyword,index = false)
  private String z;
  @Field(type = FieldType.Keyword,index = false)
  private String fid;
  @Field(type = FieldType.Keyword)
  private String time;
  @Field(type = FieldType.Keyword,index = false)
  private String photo;
  @Field(type = FieldType.Keyword,index = false)
  private String photolocal;
  @Field(type = FieldType.Keyword,index = false)
  private String photo2;
  @Field(type = FieldType.Keyword,index = false)
  private String photolocal2;
  @Field(type = FieldType.Keyword,index = false)
  private String thumbnail;
  @Field(type = FieldType.Keyword,index = false)
  private String thumbnaillocal;
  @Field(type = FieldType.Keyword,index = false)
  private String url;
  @Field(type = FieldType.Keyword,index = false,value="object_type")
  private String objectType;
  @Field(type = FieldType.Keyword,index = false,value="icon_type")
  private String iconType;
  @Field(value = "addTime",type = FieldType.Date, format = DateFormat.custom,pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime addTime;

  private static final long serialVersionUID = 1L;
}
