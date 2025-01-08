package com.tgy.rtls.data.entity.update;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import net.sf.json.JSONObject;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author rtls
 * @since 2020-11-18
 */
@Data
@TableName("bsfirmware")
public class BsfirmwareEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Long bsid;
    private Integer armupdatestate;
    private Integer uwbupdatestate;
    private String product;
    private String   pid;
    private String    fru;
    private String      sn;
    private String     release;
    private String      core;
    private String      qt;
    private String      ucb1;
    private String      ucb2;

    @Override
    public String toString() {
        return JSONObject.fromObject(this).toString();
    }
}
