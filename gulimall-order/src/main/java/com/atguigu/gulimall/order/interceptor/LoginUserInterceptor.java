package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class LoginUserInterceptor implements HandlerInterceptor{


    public static ThreadLocal<MemberResponseVo> LoginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        MemberResponseVo attribute = (MemberResponseVo)request.getSession().getAttribute(AuthServerConstant.LOGIN_USER);
        if(attribute != null){
            LoginUser.set(attribute);
            return true;
        }else{
            //没登陆就去登录
            request.getSession().setAttribute("msg","请先进行登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }

    }
}
