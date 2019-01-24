package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author York
 * @create 2018-11-14 19:29
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    /**
     * 进入所有控制器之前需要执行的方法
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从url中获取token信息
        String token = request.getParameter("newToken");
        //用户刚登陆获取url中的token信息并将token保存到cookie中
        if (token!=null){
            //用户登录后将token信息保存至cookie中
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }
        if (token==null){
            //如果获取不到token信息,有两种情况,用户已登录token信息存放在cookie中,或者是用户未登录
            token=CookieUtil.getCookieValue(request,"token",false);
        }
        if (token!=null){
            //如果cookie中存在对应的token,则获取token信息
            Map userMapByToken = getUserMapByToken(token);
            String nickName = (String)userMapByToken.get("nickName");
            request.setAttribute("nickName",nickName);
        }

        //根据注解判断跳转的页面是否需要先登录,handler可以获取到方法上的注解
        //先获取注解@LoginRequire
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire loginRequireAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        if (loginRequireAnnotation!=null){
            //获取currentIp
            String remoteAddr = request.getHeader("x-forwarded-for");
            //使用远程工具类调用passport-web中的认证控制器
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + remoteAddr);
            //如果可以从redis中获取用户信息证明已经登录
            if ("success".equals(result)){
                Map map = getUserMapByToken(token);
                String userId = (String)map.get("userId");
                request.setAttribute("userId",userId);
                return true;
            }else {
                //认证控制器返回fail,又加了@LoginRequire,则必须跳转至登录页面让用户进行登录
                if (loginRequireAnnotation.autoRedirect()){
                    //用户未登录,跳转至登录页面之前需要将跳转之前的url获取到拼接到登录的url后面以便登陆之后跳转回之前页面
                    String requestURL = request.getRequestURL().toString();
                    String encodeURL = URLEncoder.encode(requestURL);
                    response.sendRedirect( WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                    return false;

                }
            }
        }


        return true;
    }

    private Map getUserMapByToken (String token){
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        //将获取的tokenUserInfo进行解码
        byte[] tokenBytes = base64UrlCodec.decode(tokenUserInfo);
        String tokenJson = null;
        try {
            tokenJson = new String(tokenBytes,"UTF-8");
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Map map = JSON.parseObject(tokenJson,Map.class);
        return map;

    }

    //进入控制器之后,返回视图之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    //返回视图之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
    }


}
