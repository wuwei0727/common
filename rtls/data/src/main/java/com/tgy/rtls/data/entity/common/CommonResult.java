package com.tgy.rtls.data.entity.common;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.entity.common
 * @date 2020/10/14
 */
@Data
@NoArgsConstructor
public class CommonResult<T> {
    private Integer code;//状态码
    private String message;//提示
    private T data;//数据
    private Integer userId;

    public CommonResult(Integer code, String message) {
        this(code,message,null);
    }

    public CommonResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public CommonResult(Integer code, String message, T data, Integer userId) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.userId = userId;
    }
}
