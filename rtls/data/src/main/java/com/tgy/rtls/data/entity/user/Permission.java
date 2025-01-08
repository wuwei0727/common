package com.tgy.rtls.data.entity.user;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author 许强
 * @Package com.tuguiyao.bean.user
 * @date 2019/9/24
 */
@Data
@ToString
public class Permission implements Serializable {
    private Integer id;//主键
    private String name;//权限名称
    private String url;//地址
    private String permission;//权限字符串，role:add
    private String parentid;//父编号
    private String parentids;
    private int enabled;//是否启用
}
