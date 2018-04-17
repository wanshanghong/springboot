package com.xwbing.handler;

import com.alibaba.fastjson.JSON;
import com.xwbing.configuration.DispatcherServletConfig;
import com.xwbing.util.CommonDataUtil;
import com.xwbing.util.RestMessage;
import com.xwbing.util.ThreadLocalUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 说明:  登录拦截器
 * 项目名称: boot-module-demo
 * 创建时间: 2017/5/10 16:36
 * 作者:  xiangwb
 */
public class LoginInterceptor extends HandlerInterceptorAdapter {
    private final Logger logger = LoggerFactory.getLogger(DispatcherServletConfig.class);
    private static final Set<String> set = new HashSet<>();//拦截器白名单

    static {
        //映射到登录页面，不拦截
        set.add("/");
        //映射swagger文档
        set.add("/doc");
        //验证码
        set.add("/captcha");
        //swagger
        set.add("/v2/api-docs");
        set.add("/swagger-resources");
        set.add("/configuration/ui");
        set.add("/configuration/security");
        //德鲁伊监控
        set.add("/druid");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String servletPath = request.getServletPath();
        if (!set.contains(servletPath) && !servletPath.contains("login") && !servletPath.contains("test")) {
            String token = request.getHeader("token");
            if (StringUtils.isEmpty(token)) {
                getOutputStream(response, "token不能为空");
                return false;
            } else {
                if (CommonDataUtil.getToken(token) != null) {
                    ThreadLocalUtil.setToken(token);
                    return true;
                } else {
                    logger.error("用户未登录");
                    getOutputStream(response, "请先登录");
                    //未登录，重定向到登录页面
//                response.sendRedirect("/login.html");
                    return false;
                }
            }
        }
        return true;
    }

    private void getOutputStream(HttpServletResponse response, String msg) {
        try {
            OutputStream outputStream = response.getOutputStream();
            RestMessage restMessage = new RestMessage();
            restMessage.setMessage(msg);
            outputStream.write(JSON.toJSONString(restMessage).getBytes("utf-8"));
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}