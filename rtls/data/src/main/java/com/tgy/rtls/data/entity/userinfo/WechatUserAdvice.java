package com.tgy.rtls.data.entity.userinfo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author rtls
 * @since 2020-11-13
 */
@Data

public class WechatUserAdvice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    private Integer userid;
    private Integer functionId;
    private Short type;//0不满意  1：一般  2：满意
    private String content;//内容
    private String comment;
    private String more;




}
