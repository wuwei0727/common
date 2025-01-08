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
@TableName("tagcheckbsid")
public class TagcheckbsidEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @Descrip(value ="批次号")
    public Long tagcheckid;
    @Descrip(value ="分站ID")
    public Integer bsid;
    @Descrip(value = "检卡总数")
    public Integer totaldistinct;
    @Descrip(value ="目标总数")
    public Integer total;
    @Descrip(value ="漏读率")
    public Double lackpercent;
    @Descrip(value ="开始检卡时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date start;
    @Descrip(value ="结束检卡时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date end;
    @Descrip(value ="检卡时长(ms)")
    public Integer period ;
    @Descrip(value ="漏读详情")
    public String lackedetail;
    public Integer state;

    @Descrip(value ="当前分站检卡数")
    public Integer currenttotal;
    @Descrip(value = "当前分站检卡详情")
    public String currentdetail;

    public Integer finishtype;
    public Integer checktime;
    public Integer type;//类型0单站检卡，1多站检卡，2定位检卡



}
