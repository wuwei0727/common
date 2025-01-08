package com.tgy.rtls.web.shiro;

import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @BelongsProject: rtls
 * @BelongsPackage: com.tgy.rtls.web.shiro
 * @Author: wuwei
 * @CreateTime: 2022-08-12 17:52
 * @Description: TODO
 * @Version: 1.0
 */
@RestControllerAdvice
public class NoPermissionException {
    @ResponseBody
    @ExceptionHandler(UnauthorizedException.class)
    public void handleShiroException(Exception ex, HttpServletResponse response, HttpServletRequest request) throws IOException, ServletException {
        //response.sendRedirect("/page/unauth");
        response.setStatus(401);
        request.getRequestDispatcher("/page/unauth").forward(request, response);
    }
}
