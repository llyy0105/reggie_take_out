package com.ly.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.ly.reggie.common.BaseContext;
import com.ly.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Manager;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @Description  检查用户是否已经完成登录
 * @Author ly
 * @create 2023-02-08 20:08
 */
@Slf4j
@WebFilter(filterName = "LoginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 1.获取本次请求的uri
        String requestURI = request.getRequestURI();
        log.info("拦截到请求：{}",requestURI);

        // 定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };

        // 2.判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        // 3.如果不需要处理，则直接放行
        if(check){
            log.info("本次请求{}不需要处理",requestURI);
            chain.doFilter(request,response);
            return;
        }

        // 4-1.判断登陆状态，如果已登录，则直接放行
        HttpSession session = request.getSession();
        if (session.getAttribute("employee") != null){
            log.info("用户已登录，用户id为：{}",session.getAttribute("employee"));

            Long empId = (Long) session.getAttribute("employee");
            BaseContext.setCurrendId(empId);

            chain.doFilter(request,response);
            return;
        }
        // 4-2.判断登陆状态，如果已登录，则直接放行
        if (session.getAttribute("user") != null){
            log.info("用户已登录，用户id为：{}",session.getAttribute("user"));

            Long userId = (Long) session.getAttribute("user");
            BaseContext.setCurrendId(userId);

            chain.doFilter(request,response);
            return;
        }

        // 5.如果未登录则返回未登录结果，通过输出流向客户端页面响应数据
        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    // 路径匹配，检查此次请求是否需要放行
    public boolean check(String[] urls,String requestURI){
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if(match){
                // 匹配到路径，放行
                return true;
            }
        }
        // 没有匹配到路径，不放行
        return false;
    }
}
