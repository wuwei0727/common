package com.tgy.rtls.data.entity.xianjiliang;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.user
 * @date 2020/10/13
 * 人员
 */
@Data
@ToString
public class TypeTime implements Serializable {
    /*private Integer id;//*/
    //private String type;//样品类型
    private String typeName ;//样品类别名称
    private String typeId;//样品类比
    private Float time;//单个样品检测时间
    private Integer total;//检验总数
  /*  private Integer unchecked;//未检验
    private Integer checked;//已经检验*/
    /*
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkTime;//应检时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date finishTime;//检查结束时间
*/


}
