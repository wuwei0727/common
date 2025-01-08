package com.tgy.rtls.data.entity.check;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tgy.rtls.data.common.Descrip;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author rtls
 * @since 2020-11-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tagchecklocation")
public class TagchecklocationEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    @Descrip(value ="批次号")
    public Long checkbsid;
    @Descrip(value ="标签号")
    public Integer tagid;
    @Descrip(value ="基站编号")
    public Integer bsid;
    @Descrip(value ="定位时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date time;
    @Descrip(value ="坐标x")
    public Float x;
    @Descrip(value ="坐标y")
    public Float y;
    @Descrip(value ="坐标z")
    public Float z;


}
