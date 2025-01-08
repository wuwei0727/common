package com.tgy.rtls.web.shiro;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.park.ParkingCompany;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.entity.user.Permission;
import com.tgy.rtls.data.mapper.user.PermissionMapper;
import com.tgy.rtls.data.service.user.impl.MemberService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
*@Author: wuwei
*@CreateTime: 2022/12/10 16:25
*/
public class MyShiroRealm extends AuthorizingRealm {
    @Autowired(required = false)
    private MemberService memberService;
    @Autowired(required = false)
    private PermissionMapper permissionMapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${rtls.shiro.salt}")
    private String salt;

    @Override
    public String getName(){
        return "WEBRealm";
    }

    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SimpleAuthorizationInfo authorizationInfo=new SimpleAuthorizationInfo();
        Object object= principalCollection.getPrimaryPrincipal();
        SecurityUtils.getSubject().getSession().setTimeout(10000000L);
        if(object instanceof Member){
            Member member=(Member) object;
            for (Permission p:member.getPermissions()){
                authorizationInfo.addStringPermission(p.getPermission());
            }

        }else if(object instanceof ParkingCompany){
            ParkingCompany parkingCompany=(ParkingCompany) object;
            List res=  permissionMapper.findRolePermissions(parkingCompany.getId());
            authorizationInfo.addStringPermissions(res);
        }
        return authorizationInfo;
    }
    //认证登录
    /*主要是用来进行身份认证的，也就是说验证用户输入的账号和密码是否正确*/
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
            logger.info("调用web端realm");
            //获取用户输入的账号
            String phone = (String) token.getPrincipal();
            Object dsda = token.getCredentials();
            int length = Array.getLength(dsda);
            char[] os = new char[length];
            for (int i = 0; i < os.length; i++) {
                os[i] = (char) Array.get(dsda, i);
            }
            String ss = String.valueOf(os);

            //通过username从数据库中查找User对象
            Member member = memberService.findByPhone(phone.replace(" ", ""));
            if ((NullUtils.isEmpty(member) && !"wechat".equals(ss))) {
                return null;
            }
            if (member != null && member.getEnabled() == 0) {
                throw new DisabledAccountException(String.valueOf(new CommonResult<>(500, LocalUtil.get(KafukaTopics.AccountNotEna))));
            }
            String password = null;
            Object object = null;
            if (member != null) {
                password = member.getPassword();
                object = member;
            }
            if ("wechat".equals(ss)) {

                UsernamePasswordToken object1 = new UsernamePasswordToken();
                object1.setPwd("wechat");
                object1.setUserId(phone);
                object = object1;
                password = "0af7f894fc99b39f931ccb0a6a8e4418";
            }

            ByteSource byteSource = ByteSource.Util.bytes(salt);
            SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(object, password, byteSource, getName());
            Subject subject = SecurityUtils.getSubject();
            String host = subject.getSession().getHost();
            if (member != null) {
                memberService.addLonginRecord(member.getUid(),member.getPhone(), host);
            }
            if(!NullUtils.isEmpty(member)&&object instanceof Member){
                SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                memberService.updateAddTime(member.getUid(),dateFormat.format(new Date()));
            }
            return authenticationInfo;
        }

    //这是一个main方法，程序的入口
    public static void main(String[] args){
        ByteSource byteSource = ByteSource.Util.bytes("lrr");
        SimpleHash simpleHash = new SimpleHash("MD5","123456",byteSource,16);
        System.out.println(simpleHash);
    }

}
