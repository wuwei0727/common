package com.tgy.rtls.location.model;

import lombok.Data;

@Data
public class Safebelt {

    private Long id;					//自增长ID
    private Long code;				//安全带编码
    private Long project_id;		//项目
    private short online_status;  //在线状态
    private String project_name;
}
