package com.tgy.rtls.data.entity.view;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.entity.view
 * @Author: wuwei
 * @CreateTime: 2024-06-14 20:38
 * @Description: TODO
 * @Version: 1.0
 */
@Data
@ApiModel(description = "大屏数据二次更新")
public class UserVo {
    private Long newUserTotal;//新增用户总数
    private Long userIds;
    private String loginTimes;
    public UserVo(Long userTotal) {
        this.newUserTotal = userTotal;
    }
    public UserVo(Long userIds, String loginTimes) {
        this.userIds = userIds;
        this.loginTimes = loginTimes;
    }
}
