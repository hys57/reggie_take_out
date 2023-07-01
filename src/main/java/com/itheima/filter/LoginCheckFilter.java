package com.itheima.filter;


import com.alibaba.fastjson.JSON;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.AnnotatedArrayType;

//过滤器
@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器
    public static final AntPathMatcher PATH_MATCHER=new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request1 = (HttpServletRequest) request;
        HttpServletResponse response1 = (HttpServletResponse) response;

        //获取本次请求的URI
        String requestURI=request1.getRequestURI();
        //log.info("拦截到请求：{}",request1.getRequestURI());

        //定义不需要拦截的路径
        String[] urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",//发送请求
                "/user/login"//登录
        };

        //匹配结果
        boolean check = check(urls, requestURI);

        //不需要处理，直接放行
        if(check){
            //log.info("不处理");
            chain.doFilter(request1,response1);
            return;
        }

        //账号已登录，直接放行
        if(request1.getSession().getAttribute("employee")!=null){
            //log.info("不处理：{}",request1.getSession().getAttribute("employee"));
            //将用户id保存
            Long empId=(Long)request1.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            chain.doFilter(request1,response1);
            return;
        }

        //账号已登录，直接放行
        if(request1.getSession().getAttribute("user")!=null){
            //log.info("不处理：{}",request1.getSession().getAttribute("employee"));
            //将用户id保存
            Long userId=(Long)request1.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            chain.doFilter(request1,response1);
            return;
        }

        //未登录则向客户端页面响应数据
        //log.info("用户未登录");
        response1.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;


    }

    //路径匹配
    public boolean check(String[] urls,String requestURI){
        for(String url :urls){
            boolean match=PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }
}
