package com.atguigu.gmall.usermanage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserInfoService;
import com.atguigu.gmall.usermanage.mapper.UserAddressMapper;
import com.atguigu.gmall.usermanage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author York
 * @create 2018-10-31 18:21
 */
@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    public String USERKEY_PREFIX="user:";
    public String USERINFOKEY_SUFFIX=":info";
    public int USERKEY_TIMEOUT=60*60*24;


    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> getUserAddressList(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        List<UserAddress> addressList = userAddressMapper.select(userAddress);
        return addressList;

    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        //密码进行加密再匹配
        String passwd = userInfo.getPasswd();
        String newPassword = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPassword);
        UserInfo userInfo1 = userInfoMapper.selectOne(userInfo);
        //将查询出来的对象放入redis
        if (userInfo1!= null){
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(USERKEY_PREFIX+ userInfo1.getId()+USERINFOKEY_SUFFIX,USERKEY_TIMEOUT, JSON.toJSONString(userInfo1));
            jedis.close();
        }
        return userInfo1;
    }

    @Override
    public UserInfo verify(String userId) {
        //先去redis中查询是否有缓存
        Jedis jedis = redisUtil.getJedis();
        String key = USERKEY_PREFIX + userId + USERINFOKEY_SUFFIX;
        String userJson = jedis.get(key);
        //判断是否从redis中获取到了数据
        if(userJson!=null){
            //如果redis中有对应的用户信息则刷新该用户信息的时效
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }
        return null;

    }
}
