package com.tgy.rtls.web.shiro;

import at.pollux.thymeleaf.shiro.dialect.ShiroDialect;
import com.tgy.rtls.web.config.AuthenticationFilters;
import com.tgy.rtls.web.config.CORSFilter;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authc.pam.AtLeastOneSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.Filter;
import java.util.*;


/**
 * @author 许强
 * @Package com.tuguiyao.util
 * @date 2019/9/24
 */
@Configuration
public class ShiroConfig {
    /**
     * 加密类型
     */
    @Value("${rtls.shiro.algorithmName}")
    private String algorithmName;

    /**
     * 加密次数
     */
    @Value("${rtls.shiro.hashIterations}")
    private Integer hashIterations;
    @Value("${rtls.shiro.salt}")
    private String salt;
    @Value("${rtls.shiro.unauthorizedUrl}")
    private String unauthorizedUrl;

    @Bean
    public ShiroFilterFactoryBean shiroFilter(@Qualifier("securityManagers") SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();

        shiroFilterFactoryBean.setSecurityManager(securityManager);
        //拦截器.
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();
        // 配置不会被拦截的链接 顺序判断
        /**
         * 添加shiro 内置过滤器
         * Shiro内置过滤器，可以实现权限相关的拦截器
         *    常用的过滤器：
         *       anon: 无需认证（登录）可以访问
         *       authc: 必须认证才可以访问
         *       user: 如果使用rememberMe的功能可以直接访问
         *       perms： 该资源必须得到资源权限才可以访问
         *       roles: 该资源必须得到角色权限才可以访问
//         */
//       filterChainDefinitionMap.put("/wxInfrared/getInfraredSel", "anon");
//       filterChainDefinitionMap.put("/wxGateway/getGatewaySel", "anon");

//        filterChainDefinitionMap.put("/park/queryUserPositions", "anon");
//         filterChainDefinitionMap.put("/userCompanyMap/**", "anon");
//         filterChainDefinitionMap.put("/checkParkingStatus/**", "anon");
//         filterChainDefinitionMap.put("/carPlate/**", "anon");
//         filterChainDefinitionMap.put("/placeUnlockRecords/**", "anon");
//         filterChainDefinitionMap.put("/timePeriod/**", "anon");
//         filterChainDefinitionMap.put("/infrared/updateLifetime", "anon");
//         filterChainDefinitionMap.put("/sub/updateLifetime", "anon");
         filterChainDefinitionMap.put("/wxGateway/beaconPos", "anon");
         filterChainDefinitionMap.put("/park/exportFloorLockExcel/**", "anon");


         filterChainDefinitionMap.put("/es/**", "anon");
         filterChainDefinitionMap.put("/data-import/**", "anon");
        // filterChainDefinitionMap.put("/promoter_qr_code/**", "anon");
        // filterChainDefinitionMap.put("/map/getMap2dSel", "anon");
         filterChainDefinitionMap.put("/variable_operational_data/**", "anon");
         filterChainDefinitionMap.put("/importExportDeviceInfo/**", "anon");
         filterChainDefinitionMap.put("/importExportDeviceInfo/**/**", "anon");



       filterChainDefinitionMap.put("/camera/checkDetectionExceptions", "anon");
       filterChainDefinitionMap.put("/remind/**", "anon");
        filterChainDefinitionMap.put("/park/testt", "anon");
        filterChainDefinitionMap.put("/task/**", "anon");
        filterChainDefinitionMap.put("/MP_verify_XytFryEsZhlUx4vp.txt", "anon");
        filterChainDefinitionMap.put("/product/**/", "anon");
        filterChainDefinitionMap.put("/page/view.html", "anon");
        filterChainDefinitionMap.put("/smsQuota/**/", "anon");
        filterChainDefinitionMap.put("/es/**/", "anon");
        filterChainDefinitionMap.put("/peb/**/", "anon");
        filterChainDefinitionMap.put("/test/**", "anon");
        filterChainDefinitionMap.put("/placeVideoDetection/**", "anon");

        filterChainDefinitionMap.put("/aliPay/**/", "anon");
        filterChainDefinitionMap.put("/wxOAuth2/**", "anon");
        filterChainDefinitionMap.put("/wechat/**/", "anon");
        filterChainDefinitionMap.put("/page/pay/wechatJSApiPay.html", "anon");
        filterChainDefinitionMap.put("/UWB/MP_verify_XytFryEsZhlUx4vp.txt", "anon");
        filterChainDefinitionMap.put("/UWB2/MP_verify_XytFryEsZhlUx4vp.txt", "anon");




        filterChainDefinitionMap.put("/api/events/capture", "anon");
        filterChainDefinitionMap.put("/wechat/getPlaceDetail", "anon");
        filterChainDefinitionMap.put("/WxMiniApp/getParkingElevatorBindingById", "anon");
        filterChainDefinitionMap.put("/park/delPlaceRecycleById", "anon");
        filterChainDefinitionMap.put("/park/getWechatPlaceByCompanyName", "anon");
        filterChainDefinitionMap.put("/mapHotspotData/saveTodo2", "anon");
        filterChainDefinitionMap.put("/mapHotspotData/getAndDeleteTodos2", "anon");
        filterChainDefinitionMap.put("/mapHotspotData/saveTodo", "anon");
        filterChainDefinitionMap.put("/mapHotspotData/getAndDeleteTodos", "anon");
        filterChainDefinitionMap.put("/mapHotspotData/getHotSearchByMap", "anon");
        filterChainDefinitionMap.put("/mapHotspotData/addHotSearch", "anon");
        filterChainDefinitionMap.put("/mapHotspotData/saveData", "anon");
        filterChainDefinitionMap.put("/mapHotspotData/getData", "anon");
        filterChainDefinitionMap.put("/mapHotspotData/removeElementsWithScoreOne", "anon");
        filterChainDefinitionMap.put("/findCarH5/**", "anon");
        filterChainDefinitionMap.put("/uniappWebView/**", "anon");
        filterChainDefinitionMap.put("/wxSmallAPP/addTimePeriodConfig", "anon");
        filterChainDefinitionMap.put("/api/**", "anon");
        filterChainDefinitionMap.put("/lockDevice/**", "anon");
        filterChainDefinitionMap.put("/thirdParty/thirdPartyCheckAndCall2", "anon");
         filterChainDefinitionMap.put("/thirdParty/thirdPartyCheckAndCall", "anon");
        filterChainDefinitionMap.put("/WxMiniApp/**", "anon");
        filterChainDefinitionMap.put("/page/jumpwx.html", "anon");
        filterChainDefinitionMap.put("/WxMiniApp/getShortLink", "anon");
        filterChainDefinitionMap.put("/flc/getFloorLockInfoInfoByMapId/**", "anon");
        filterChainDefinitionMap.put("/park/getShangjia1", "anon");
        filterChainDefinitionMap.put("/park/getCompany1", "anon");
        filterChainDefinitionMap.put("/park/getSimulateTrail", "anon");
        filterChainDefinitionMap.put("/promoter_log/**", "anon");
        filterChainDefinitionMap.put("/emsbp/**", "anon");
        filterChainDefinitionMap.put("/camera/**", "anon");
        filterChainDefinitionMap.put("/mapBuild/getMapBuild2", "anon");
        filterChainDefinitionMap.put("/parkUniversal/**", "anon");
        filterChainDefinitionMap.put("/park/reservePlace", "anon");
        filterChainDefinitionMap.put("/crossLevelCorridor/getConditionalQuery2", "anon");
        filterChainDefinitionMap.put("/park/getPlaceExit2", "anon");
        filterChainDefinitionMap.put("/page/placeChoose.html", "anon");
        filterChainDefinitionMap.put("/page/park/**", "anon");
        filterChainDefinitionMap.put("/fMap/**", "anon");
        filterChainDefinitionMap.put("/placeVideoDetection/getAllPlaceVideoDetectionOrConditionQuery", "anon");
        filterChainDefinitionMap.put("/placeVideoDetection/getPlaceByLicense", "anon");
        filterChainDefinitionMap.put("/park/updatePlaceDataByPlaceId", "anon");
        filterChainDefinitionMap.put("/ajb/**", "anon");
        filterChainDefinitionMap.put("/rtls/**", "anon");
        filterChainDefinitionMap.put("/mapPathLabel/getMapPathLabel2", "anon");
        filterChainDefinitionMap.put("/appletsWebSocket/**", "anon");
        filterChainDefinitionMap.put("/page/bigView.html", "anon");
        filterChainDefinitionMap.put("/page/jump.html", "anon");
        filterChainDefinitionMap.put("/wxSmallAPP/getMap2dId/**", "anon");
        filterChainDefinitionMap.put("/beaconTest/**", "anon");
        filterChainDefinitionMap.put("/view/**", "anon");

        filterChainDefinitionMap.put("/park/getShangjiaType", "anon");
        filterChainDefinitionMap.put("/wxSmallAPP/**", "anon");
        filterChainDefinitionMap.put("/upload/**", "anon");
        filterChainDefinitionMap.put("/dispark/**", "anon");
        filterChainDefinitionMap.put("/file/**", "anon");
        filterChainDefinitionMap.put("/css/**", "anon");
        filterChainDefinitionMap.put("/js/**", "anon");
        filterChainDefinitionMap.put("/image/**", "anon");
        filterChainDefinitionMap.put("/websocket/**", "anon");
        filterChainDefinitionMap.put("/*Api/**", "anon");
        filterChainDefinitionMap.put("/sub/getSubNum/**", "anon");
        filterChainDefinitionMap.put("/sub/beacondata/**", "anon");
        filterChainDefinitionMap.put("/bsconfig/**", "anon");
        filterChainDefinitionMap.put("/page/page", "anon");//页面
        filterChainDefinitionMap.put("/sinope/*", "anon");// 接口
        filterChainDefinitionMap.put("/wechat/*", "anon");// 接口
        filterChainDefinitionMap.put("/wx/*", "anon");// 接口
        filterChainDefinitionMap.put("/hot/**", "anon");// 接口
        filterChainDefinitionMap.put("/collect/*", "anon");// 接口
        filterChainDefinitionMap.put("/park/update/status/*", "anon");// nb设备接口
        filterChainDefinitionMap.put("/collect/getbeaconInfo", "anon");// 信标信息查询接口
        filterChainDefinitionMap.put("/wechat/getParkList", "anon");// 获取附近停车场距离
        //配置退出 过滤器,其中的具体的退出代码Shiro已经替我们实现了
        filterChainDefinitionMap.put("/page/unauth", "anon");
        //<!-- 过滤链定义，从上向下顺序执行，一般将/**放在最为下边 -->:这是一个坑呢，一不小心代码就不好使了;
        //<!-- authc:所有url都必须认证通过顶点坐标才可以访问; anon:所有url都都可以匿名访问-->
//        filterChainDefinitionMap.put("/**", "anon");
        filterChainDefinitionMap.put("/**", "authc");
////        /*shiro cross-domain*/
//        Map<String, Filter> filters = shiroFilterFactoryBean.getFilters();
//        filters.put("authc", new AuthenticationFilters());
//        shiroFilterFactoryBean.setFilters(filters);

        // 如果不设置默认会自动寻找Web工程根目录下的"/login.jsp"页面12
        shiroFilterFactoryBean.setLoginUrl("/page/tologin");
        //未授权界面;
        shiroFilterFactoryBean.setUnauthorizedUrl(unauthorizedUrl);
        // 登录成功后要跳转的链接
        shiroFilterFactoryBean.setSuccessUrl("/page/home");
        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        return shiroFilterFactoryBean;
    }

    /**
     * 权限不足跳转页面
     * @return
     */
    @Bean
    public SimpleMappingExceptionResolver resolver() {
        SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
        Properties properties = new Properties();
        properties.setProperty("org.apache.shiro.authz.UnauthorizedException", unauthorizedUrl);
        resolver.setExceptionMappings(properties);
        return resolver;
    }

    /**
     * 身份认证realm; (这个需要自己写，账号密码校验；权限等)
     *
     * @return
     */
    @Bean
    public MyShiroRealm myShiroRealm(@Qualifier("hashedCredentialsMatcher") HashedCredentialsMatcher hashedCredentialsMatcher) {
        MyShiroRealm myShiroRealm = new MyShiroRealm();
//        //加密
        myShiroRealm.setCredentialsMatcher(hashedCredentialsMatcher);
        return myShiroRealm;

    }

    /**
     * 加密方式
     */
    @Bean(name = "hashedCredentialsMatcher")
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher hashedCredentialsMatcher = new HashedCredentialsMatcher();
        hashedCredentialsMatcher.setHashIterations(hashIterations);//编码次数
        hashedCredentialsMatcher.setHashAlgorithmName(algorithmName);//设置哈希算法名称/类型
        return hashedCredentialsMatcher;
    }

    /**
     * 身份认证smallApp realm; (这个需要自己写，账号密码校验；权限等)
     *
     * @return
     */
    @Bean
    public WechatRealm wechatRealm(@Qualifier("hashedCredentialsMatcher") HashedCredentialsMatcher hashedCredentialsMatcher) {

        WechatRealm wechatRealm = new WechatRealm();
        wechatRealm.setCredentialsMatcher(hashedCredentialsMatcher);
        return wechatRealm;
    }

    /**
     * 配置ShiroDialect，用于thymeleaf和shiro标签配合使用 不加这个前端使用shiro标签不会生效
     */
    @Bean(name = "shiroDialect")
    public ShiroDialect shiroDialect() {
        MyShiroRealm myShiroRealm = new MyShiroRealm();
        myShiroRealm.setAuthorizationCachingEnabled(true);

        WechatRealm wechatRealm = new WechatRealm();
        wechatRealm.setAuthorizationCachingEnabled(true);
        return new ShiroDialect();
    }

    /*
     * 开启shiro aop注解支持
     * 使用代理方式；所以需要开启代码支持
     * 开启Shiro注解通知器
     * */
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }

    //异常处理
    @Bean(name = "simpleMappingExceptionResolver")
    public SimpleMappingExceptionResolver createSimpleMappingExceptionResolver() {
        SimpleMappingExceptionResolver r = new SimpleMappingExceptionResolver();
        Properties mappings = new Properties();
        mappings.setProperty("DatabaseException", "databaseError");//数据库异常处理
        mappings.setProperty("UnauthorizedException", "/page/unauth");
        r.setExceptionMappings(mappings);
        r.setDefaultErrorView("error");
        r.setExceptionAttribute("ex");
        return r;
    }

    //配置核心安全事务管理器 defaultWebSecurityManager
    @Bean(name = "securityManagers")
    public SecurityManager securityManager(@Qualifier("myShiroRealm") MyShiroRealm myShiroRealm, @Qualifier("wechatRealm") WechatRealm wechatRealm) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        //系统自带的Realm管理，主要针对多realm
        securityManager.setAuthenticator(modularRealmAuthenticator());
        //设置realm
        List<Realm> realms = new ArrayList<>();
        realms.add(myShiroRealm);
        realms.add(wechatRealm);
        securityManager.setRealms(realms);
        return securityManager;
    }

    /**
     * 系统自带的Realm管理，主要针对多realm
     */
    @Bean
    public ModularRealmAuthenticator modularRealmAuthenticator() {
        //扩展父类原方法，捕获原始异常
        MultiRealmAuthenticator modularRealmAuthenticator = new MultiRealmAuthenticator();
        //设置多个realm认证策略，一个成功即跳过其它的
        modularRealmAuthenticator.setAuthenticationStrategy(new AtLeastOneSuccessfulStrategy());
        return modularRealmAuthenticator;
    }

    /**
     * 开启Shiro的注解(如@RequiresRoles,@RequiresPermissions),需借助SpringAOP扫描使用Shiro注解的类,并在必要时进行安全逻辑验证
     * 配置以下两个bean(DefaultAdvisorAutoProxyCreator和AuthorizationAttributeSourceAdvisor)即可实现此功能
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        advisorAutoProxyCreator.setProxyTargetClass(true);
        return advisorAutoProxyCreator;
    }
}