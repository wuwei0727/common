package com.tgy.rtls.web.shiro;

import lombok.Data;
import lombok.ToString;
import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * @BelongsProject: 智慧停车场
 * @BelongsPackage: com.tgy.rtls.web.shiro
 * @Author: wuwei
 * @CreateTime: 2022-07-29 12:55
 * @Description: TODO
 * @Version: 1.0
 */
public class UserToken extends UsernamePasswordToken {
    //登录类型，判断网页登录，小程序登录
    private String loginType;

    public UserToken(final String username, final String password,String loginType) {
        super(username,password);
        this.loginType = loginType;
    }

    public String getLoginType() {
        return loginType;
    }
    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
}
