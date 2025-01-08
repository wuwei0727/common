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
 * @since 2020-11-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("bserrorcodetestrecord")
public class BserrorcodetestrecordEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    public Long id;
    @Descrip(value ="批次号")
    public Long tagcheckid;
    @Descrip(value ="已发送数据包")
    public Integer sendnum;
    @Descrip(value ="返回数据包")
    public Integer receivenum;
    @Descrip(value ="错包数据包")
    public Integer errornum;
    @Descrip(value ="误码率")
    public Double errorrate;
    @Descrip(value ="丢包数")
    public Integer lost;
    @Descrip(value ="丢包率")
    public Double lostrate;
    @Descrip(value ="开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date start;
    @Descrip(value ="结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    public Date end;

    public Short state;
    @Descrip(value ="基站ID")
    public Integer bsid;
    @Descrip(value ="发包数")
    public Integer testcount;
    @Descrip(value ="测试间隔(ms)")
    public Integer testinterval;



}
