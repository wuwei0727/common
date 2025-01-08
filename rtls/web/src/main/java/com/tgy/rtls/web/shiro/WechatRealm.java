package com.tgy.rtls.web.shiro;

import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.user.SysUser;
import com.tgy.rtls.data.service.user.SysUserService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @BelongsProject: 智慧停车场
 * @BelongsPackage: com.tgy.rtls.web.shiro
 * @Author: wuwei
 * @CreateTime: 2022-07-26 09:23
 * @Description: TODO
 * @Version: 1.0
 */
public class WechatRealm extends AuthorizingRealm {
    @Autowired
    private SysUserService sysUserService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${rtls.shiro.salt}")
    private String salt;
    @Override
    public String getName() {
        return "SMALLAPPRealm";
    }

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
//        Object principal=principals.getPrimaryPrincipal();
//        SysUserDemo user= (SysUserDemo) principal;
//        //3.创建SimpleAuthorizationInfo ,并设置roles属性
//        SimpleAuthorizationInfo simpleAuthorizationInfo=new SimpleAuthorizationInfo();
//        //8.3.将用户有权限的资源授权给shiro
//        Set<String> list = sysUserService.findByUserId(user.getUserId());
//        simpleAuthorizationInfo.setStringPermissions(list);//设置资源授权
//        return simpleAuthorizationInfo;
        return null;
    }

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        logger.info("调用小程序realm");
        UsernamePasswordToken userToken= (UsernamePasswordToken) token;
            /**
             * 根据登录名查询用户
             * @param loginName
             * @return
             */
            SysUser users = sysUserService.queryUser(userToken.getUsername());
            if (NullUtils.isEmpty(users)) {
                return null;
            }
            if (users.getEnable() == 0) {
                throw new DisabledAccountException();
            }

            SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            ByteSource byteSource=ByteSource.Util.bytes(salt);

            Subject subject = SecurityUtils.getSubject();
            String host = subject.getSession().getHost();
            logger.error("login success" + host);
            sysUserService.updateAddTime(users.getUserId(),dateFormat.format(new Date()));
            if (NullUtils.isEmpty(users)) {
                sysUserService.addLonginRecord(users.getUserName(), host);
            }

        return new SimpleAuthenticationInfo(users,users.getPassword(),byteSource,getName());
    }
    
    //这是一个main方法，程序的入口
    public static void main(String[] args){
        ByteSource byteSource=ByteSource.Util.bytes("lrr");
        SimpleHash simpleHash = new SimpleHash("MD5", "admin", byteSource, 16);
        System.out.println(simpleHash);

    }
}

