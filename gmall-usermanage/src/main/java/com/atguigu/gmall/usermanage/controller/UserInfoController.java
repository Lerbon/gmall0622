package com.atguigu.gmall.usermanage.controller;

import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author York
 * @create 2018-10-31 18:33
 */
@Controller
public class UserInfoController {
    @Autowired
    UserInfoService userInfoService;


    @RequestMapping("findAll")
    @ResponseBody
    public List<UserInfo> findAll(){
       return userInfoService.findAll();
    }
}
