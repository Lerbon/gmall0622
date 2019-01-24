package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * @author York
 * @create 2018-11-14 23:44
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {
        //判断购物车中是否有该商品
        CartInfo cartInfo = new CartInfo();
        //通过skuId和userId来确定唯一用户对应的唯一商品
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
        if (cartInfoExist!=null){
            //购物车中有该商品就更新商品数量
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum);
            //更新到数据库
            //给实时价格赋值
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            /*updateByPrimaryKey对你注入的字段全部更新（不判断是否为Null）
            updateByPrimaryKeySelective会对字段进行判断再更新(如果为Null就忽略更新)*/
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        }else {
            //如果不存在就保存至购物车
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            //保存到数据库
            cartInfoMapper.insertSelective(cartInfo1);
            cartInfoExist = cartInfo1;
        }
//            ----------------------
            //构建key user:userid:cart,将数据添加到redis中
            String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
            Jedis jedis = redisUtil.getJedis();
            //将对象序列化
            String cartJson = JSON.toJSONString(cartInfoExist);
            jedis.hset(userCartKey,skuId,cartJson);
            //更新购物城过期时间
            //先获取用户过期时间,然后将用户的时效设置给购物车
            String userInfoKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
            Long ttl = jedis.ttl(userInfoKey);
            jedis.expire(userCartKey,ttl.intValue());
            jedis.close();


    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        //先尝试从redis中获取
        Jedis jedis = redisUtil.getJedis();
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
//        Redis HVALS命令用于获取在存储于 key的散列的所有值。
        List<String> cartJsons = jedis.hvals(userCartKey);
        if (cartJsons!=null&&cartJsons.size()>0){
            List<CartInfo> cartInfoList = new ArrayList<>();
            //将从redis中取出来的数据进行反序列化
            for (String cartJson : cartJsons) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                //反序列化后将对象存入集合中
                cartInfoList.add(cartInfo);
            }
            //排序
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {

                    return Long.compare(Long.parseLong(o2.getId()),Long.parseLong(o1.getId()));
                }
            });
            return cartInfoList;
        }else {
            //从数据库中查询,其中商品价格可能有更新,所以需要关联sku_info表,并更新cartInfo表的cart_price
            List<CartInfo> cartInfoList = loadCartCache(userId);
            return cartInfoList;
        }

    }

    @Override
    public List<CartInfo> loadCartCache(String userId) {
       List<CartInfo> cartInfoList =  cartInfoMapper.selectCartListWithCurPrice(userId);

       if (cartInfoList==null&&cartInfoList.size()==0){
            return null;
       }
        String userCartKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        Map<String,String> map = new HashMap<>(cartInfoList.size());
        for (CartInfo cartInfo : cartInfoList) {
            String cartJson = JSON.toJSONString(cartInfo);
            map.put(cartInfo.getSkuId(),cartJson);
        }
        jedis.hmset(userCartKey,map);
        jedis.close();
        return cartInfoList;
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
       //先从数据库中查询获取购物车信息
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        for (CartInfo cartInfoCK : cartListFromCookie) {
            //定义flag判断cookie中是否有和mysql中相同的数据
            boolean isMatch = false;
            for (CartInfo cartInfoDB : cartInfoListDB) {
                //如果cookie中有和MySQL中相同的数据
                if (cartInfoDB.getSkuId().equals(cartInfoCK.getSkuId())){
                    //将商品进行合并
                    cartInfoDB.setSkuNum(cartInfoCK.getSkuNum()+cartInfoDB.getSkuNum());
                    //更新数据库
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                    isMatch = true;
                }
            }
            //cookie中没有和mysql中相同的商品,则直接添加到数据库
            if (!isMatch){
                //因为cookie中存储的购物车信息没有userId,所以需要先把userId赋值给cookie中的购物车对象
                cartInfoCK.setUserId(userId);
                //直接插入到数据库
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        //将更新完的数据更新到redis缓存中
        List<CartInfo> cartInfoList = loadCartCache(userId);
        return cartInfoList;


    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        //更新购物车中的isChecked标志
        Jedis jedis = redisUtil.getJedis();
        //获取购物车信息
        String userCartKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(userCartKey, skuId);
        //将cartJson转换为对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        //更新isChecked状态
        cartInfo.setIsChecked(isChecked);
        String cartCheckdJson  = JSON.toJSONString(cartInfo);
        jedis.hset(userCartKey,skuId,cartCheckdJson);

        //获取购物车中已选中的商品
        //专门保存购物车中被勾选的商品,方便订单结算
        String userCheckedKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CHECKED_KEY_SUFFIX;
        if (isChecked.equals("1")){
            jedis.hset(userCheckedKey,skuId,cartCheckdJson);
        }else {
            //如果没被勾选则将商品从该集合中移除
            jedis.hdel(userCheckedKey,skuId);
        }
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        //获取redis中存储被选中购物车的key
        String userCheckedKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedList = jedis.hvals(userCheckedKey);
        //将从redis中获取的被选中购物车信息转换为对象格式
        List<CartInfo> list = new ArrayList<>();
        for (String cartJson : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
            list.add(cartInfo);
        }
        return list;

    }
}
