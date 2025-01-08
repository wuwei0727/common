package com.tgy.rtls.web.config;

import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
* @BelongsProject: rtls
* @BelongsPackage: com.tgy.rtls.web.config
* @Author: wuwei
* @CreateTime: 2023-02-20 13:12
* @Description: TODO
* @Version: 1.0
*/

public class AuthenticationFilters extends BasicHttpAuthenticationFilter {

   @Override
   protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
       HttpServletResponse httpResponse = (HttpServletResponse) response;
       HttpServletRequest httpRequest = (HttpServletRequest) request;
       //无条件放行OPTIONS
       if (httpRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
           setHeader(httpRequest, httpResponse);
           return true;
       }
       return super.preHandle(request, response);
   }



   //为response设置header，实现跨域
   private void setHeader(HttpServletRequest request, HttpServletResponse response) {
       response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
       response.setHeader("Access-Control-Allow-Credentials", "true");
       response.setHeader("Access-Control-Allow-Methods","POST,PUT,GET,OPTIONS,DELETE");
       response.setHeader("Access-Control-Max-Age", "3600");
       response.setHeader("Access-Control-Allow-Headers", "content-type,x-requested-with,token,Authorization,authorization,x-auth-token,Origin,Access-Token,Accept");
       response.setHeader("Content-Type", "application/json;charset=UTF-8");
       response.setStatus(HttpStatus.OK.value());
   }
}
