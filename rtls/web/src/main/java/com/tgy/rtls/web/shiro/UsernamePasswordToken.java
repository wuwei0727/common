package com.tgy.rtls.web.shiro;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UsernamePasswordToken extends org.apache.shiro.authc.UsernamePasswordToken {
    String userId;
    String pwd;
    //登录类型，判断网页登录，小程序登录
    private String loginType;

    public UsernamePasswordToken(final String username, final String password,String loginType) {
        super(username,password);
        this.loginType = loginType;
    }

    public UsernamePasswordToken() {

    }

    public String getLoginType() {
        return loginType;
    }
    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }


}
