package com.tgy.rtls.data.entity.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author 许强
 * @Package com.tuguiyao.bean.user
 * @date 2019/10/22
 */
@Data
@ToString
public class Company implements Serializable {
    private Integer id;
    private String cname;
    private Integer memberNum;
    private String describe;
    private Integer enabled;//0不启用1启用
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date addTime;
    private List<Permission> permissions;

    private Integer uid;
    /**
     * 创建人用户id
     */
    private Integer createuId;
}
