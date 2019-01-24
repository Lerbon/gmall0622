package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

/**
 * @author York
 * @create 2018-10-31 18:03
 */
public interface UserInfoService {

    /**
     * 查询所有用户信息
     * @return
     */
    List<UserInfo> findAll();

    /**
     *根据用户id查询用户地址
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressList(String userId);

    /**
     * 用户登录的方法
     */
    UserInfo login(UserInfo userInfo);


    UserInfo verify(String userId);
}
