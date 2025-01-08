package com.tgy.rtls.data.kafukaentity;

import lombok.Data;

@Data
public class BsRange {
    Long source;
    Integer instanceId;
    Long target;
    Short type;// 0：  基站   1：标签
    int count;//次数 ，默认-1
    float dis;//距离
}
