package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.passport.config.JwtUtil;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author York
 * @create 2018-11-13 20:14
 */
@Controller
public class PassportController {

    @Value("${token.key}")
    String signKey;
    @Reference
    private UserInfoService userInfoService;

    @RequestMapping("index")
    public String index(HttpServletRequest request){
        //获取登录前的originurl
        //AuthInterceptor拦截器中已经将originURL拼接到新的url地址中,此处直接取便可
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl",originUrl);
        return "index";
    }

    @RequestMapping("login")
    @ResponseBody
    public String login(UserInfo userInfo,HttpServletRequest request){
        String salt = request.getHeader("X-forwarded-for");
        UserInfo login = userInfoService.login(userInfo);
        if (login!=null){
            //登陆之后将token返回
            Map<String, Object> map = new HashMap<>();
            map.put("userId",login.getId());
            map.put("nickName",login.getNickName());
            String token = JwtUtil.encode(signKey, map, salt);
            System.out.println("token"+ token);
            return token;

        }else {
            return "fail";
        }
    }

    /**
     * 验证用户是否登录
     * @param request
     * @return
     */
    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){
        //从request中获取token和currentIp
        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        //检查用户信息是否存在于缓存中;
        //从map 中取得 userId = map.get(userId) ,取得userId 之后，根据缓存做比较
        Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);

        if (map!=null){
            //检查该用户信息是否存在于redis中

            //从解码得到的map中获取userId
            String userId = (String)map.get("userId");
            UserInfo userInfo = userInfoService.verify(userId);
            if (userInfo!=null){
                return "success";
            }

        }
        return "fail";

    }


}
