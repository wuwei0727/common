package com.tgy.rtls.web.aspect;

import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.user.Member;
import com.tgy.rtls.data.service.user.impl.MemberService;
import org.apache.shiro.SecurityUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.data.common
 * @Author: wuwei
 * @CreateTime: 2022-08-25 11:45
 * @Description: TODO
 * @Version: 1.0
 */
//标识切面
@Aspect
@Component
public  class PermissionAspect {
    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);
    @Autowired
    private MemberService memberService;
    /**
     * 切入点
     * 切入点为包路径下的：execution(public * com.tgy.rtls.web.controller..*(..))：
     * com.tgy.rtls.web.controller包下任意类任意返回值的 public 的方法
     * <p>
     * 切入点为注解的： @annotation(MyPermission)
     * 存在 MyPermission 注解的方法
     */
    @Pointcut("@annotation(com.tgy.rtls.web.aspect.MyPermission)")
    private void permission() {

    }

    /**
     * 环绕
     * 会将目标方法封装起来
     * 具体验证业务数据
     */
    @Around("permission()")
    @ResponseBody
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        // logger.info("调用doAround方法---->开始校验权限");
        Object[] args = new Object[0];
        try {
            //获取到请求参数
            args = joinPoint.getArgs();
            Member member = (Member) SecurityUtils.getSubject().getPrincipal();

            StringBuilder  stringBuilder  = new StringBuilder ();
            if(Optional.ofNullable(member).isPresent()){
                List<Map_2d> map2dList = memberService.getMapId(member.getUid());
                if(!(map2dList.size() >1)) {
                    memberService.setId(String.valueOf(map2dList.get(0).getId()));
                }
                for (Map_2d str : map2dList) {
                    stringBuilder.append(str.getId()).append(",").toString();
                }
                Map<String, Object> fieldsName = getFieldsName(joinPoint);
                args[fieldsName.size()-1]= stringBuilder.substring(0,stringBuilder.length()-1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return joinPoint.proceed(args);
    }

    /**
     * 获取参数列表
     * @param joinPoint
     */
    private static Map<String, Object> getFieldsName(ProceedingJoinPoint joinPoint) {
        //获取参数值
        Object[] args = joinPoint.getArgs();
        ParameterNameDiscoverer pnd = new DefaultParameterNameDiscoverer();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        //获取参数名
        String[] parameterNames = pnd.getParameterNames(method);
        Map<String, Object> paramMap = new HashMap<>();
        for (int i = Objects.requireNonNull(parameterNames).length-1; i >=0; i--) {
            paramMap.put(parameterNames[i], args[i]);
        }
        return paramMap;
    }
}

