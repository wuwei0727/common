package com.tgy.rtls.web.controller.app;

import com.github.pagehelper.PageInfo;
import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.entity.common.CommonResult;
import com.tgy.rtls.data.entity.map.Map_2d;
import com.tgy.rtls.data.entity.user.SysUser;
import com.tgy.rtls.data.service.common.OperationlogService;
import com.tgy.rtls.data.service.map.Map2dService;
import com.tgy.rtls.data.service.user.SysUserService;
import com.tgy.rtls.data.tool.Constant;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: 智慧停车场
 * @BelongsPackage: com.tgy.rtls.web.controller.app
 * @Author: wuwei
 * @CreateTime: 2022-07-25 15:51
 * @Description: TODO
 * @Version: 1.0
 */
@RestController
@RequestMapping("/wxSmallAPP")
@Api(value = "微信小程序登录")
@Slf4j
public class WxSmallAppLoginController extends BasesController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OperationlogService operationlogService;
    @Autowired
    private Map2dService map2dService;
    /**
     * 设置shiroSession过期时间 ms 用于测试
     */
    @Value("${rtls.shiro.Jsession.timeout}")
    private long sessionTimeout;

    /**
     * 小程序登录接口
     *
     * @param userName 用户名
     * @param password 密码
     * @param request
     * @return
     */
    @RequestMapping("/login")
    @ResponseBody
    @ApiOperation(value = "用户登录", notes = "信标部署小程序登录的接口")
    public CommonResult<Object> login(String userName, String password, HttpServletRequest request) {
        UsernamePasswordToken token = new UsernamePasswordToken(userName, password);
        try {
            Subject subject = SecurityUtils.getSubject();
            //登陆
            SecurityUtils.getSubject().login(token);
            SysUser sysUser = (SysUser) subject.getPrincipal();
            Session session = subject.getSession();
            String host = subject.getSession().getHost();
            session.setTimeout(sessionTimeout);
            String sessionId = request.getSession().getId();
            session.setAttribute(Constant.USER_WXJSESSION_ID, sessionId);
            session.setAttribute(Constant.USER_LOGIN_TIME, System.currentTimeMillis());
            logger.info("login会话ID：" + sessionId);
            List<Map_2d> map2ds = sysUserService.getUserByIdMap(sysUser.getUserId());
            PageInfo<Map_2d> pageInfo = new PageInfo<>(map2ds);
            Map<String, Object> map = new HashMap<>();
            map.put("list", pageInfo.getList());
            map.put("sessionId", sessionId);
            operationlogService.addOperationloguser(sysUser.getUserId(), host);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.login_Success), map);
        } catch (DisabledAccountException e) {
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.AccountNotEna));
        } catch (AuthenticationException e) {
            return new CommonResult<>(400, LocalUtil.get(KafukaTopics.userNamePasswordError));
        } catch (Exception e) {
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.login_Error));
        }
    }

    @RequestMapping("/check")
    @ResponseBody
    @ApiOperation(value = "用户登录校验sessionId", notes = "信标部署小程序登录校验的接口")
    public CommonResult<Object> check() {
        Session session = SecurityUtils.getSubject().getSession();
        String sessionId = (String) session.getAttribute(Constant.USER_WXJSESSION_ID);
        logger.info("获取会话ID：" + sessionId);
        if (sessionId == null) {
            return new CommonResult<>(401, LocalUtil.get("登录过期,请重新登录"));
        }
        return new CommonResult<>(200, LocalUtil.get("校验成功"));
    }

    @RequestMapping(value = "/getMap2dId/{id}")
    @ApiOperation(value = "地图详情接口", notes = "无")
    @ApiImplicitParam(paramType = "path", name = "id", value = "2维地图id", required = true, dataType = "int")
    public CommonResult<Map_2d> getMap2dId(@PathVariable("id") Integer id) {
        try {

            Map_2d map2d = map2dService.findById(id);
            return new CommonResult<>(200, LocalUtil.get(KafukaTopics.QUERY_SUCCESS), map2d);
        } catch (Exception e) {
            e.printStackTrace();
            return new CommonResult<>(500, LocalUtil.get(KafukaTopics.SYSTEM_BUSY));
        }
    }
}
