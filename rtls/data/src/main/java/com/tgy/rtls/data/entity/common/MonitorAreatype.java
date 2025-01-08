package com.tgy.rtls.data.entity.common;

import com.tgy.rtls.data.entity.map.Area;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2020/11/5
 * 检测-->区域类型
 */
@Data
@ToString
public class MonitorAreatype implements Serializable {
    //区域类型名
    private String typeName;

    private List<Area> areas;

}
