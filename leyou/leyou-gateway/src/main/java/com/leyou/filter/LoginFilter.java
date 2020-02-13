package com.leyou.filter;

import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.leyou.utils.CookieUtils;
import com.leyou.utils.JwtUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 登录拦截过滤器
 *
 * @Component 将此配置文件注入到spring中
 *
 * @EnableConfigurationProperties 启用JwtProperties.class文件
 */
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class LoginFilter extends ZuulFilter {

    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private FilterProperties filterProperties;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 10;
    }

    /**
     * 如果返回true则拦截，反则放行
     * @return
     */
    @Override
    public boolean shouldFilter() {

        //获取白名单路径
        List<String> allowPaths = this.filterProperties.getAllowPaths();

        //初始化运行上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request对象
        HttpServletRequest request = context.getRequest();
        //获取当前请求路径
        String url = request.getRequestURL().toString();

        for (String allowPath : allowPaths) {
            //如果url包含在白名单之内则放行
            if (StringUtils.contains(url, allowPath)) {
                return false;
            }
        }
        //如果url不包含在白名单之内则过滤
        return true;
    }

    @Override
    public Object run() throws ZuulException {

        //初始化运行上下文
        RequestContext context = RequestContext.getCurrentContext();
        //获取request对象
        HttpServletRequest request = context.getRequest();

        String token = CookieUtils.getCookieValue(request, this.jwtProperties.getCookieName());

        if (StringUtils.isBlank(token)) {
            //是否转发请求
            context.setSendZuulResponse(false);
            //设置响应状态码
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }

        try {
            JwtUtils.getInfoFromToken(token,this.jwtProperties.getPublicKey());
        } catch (Exception e) {
            e.printStackTrace();
            //是否转发请求
            context.setSendZuulResponse(false);
            //设置响应状态码
            context.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }

        return null;
    }
}
