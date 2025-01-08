package com.tgy.rtls.data.entity.location;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.location
 * @date 2020/10/22
 * 原始数据
 */
@Data
@ToString
public class Batch implements Serializable {

    private String batchID;//上传基站id
    private float max;//同步源基站
    private float distance;//同步序号



}
