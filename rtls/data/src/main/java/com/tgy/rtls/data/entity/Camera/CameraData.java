package com.tgy.rtls.data.entity.Camera;

import lombok.Data;
import lombok.ToString;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.Camera
 * @Author: wuwei
 * @CreateTime: 2023-03-31 15:21
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@ToString
public class CameraData {
    private String num;
    private String license;
    private String text;
    private Integer state;
}