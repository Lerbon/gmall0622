package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.UserAddress;

import java.util.List;

/**
 * @author York
 * @create 2018-11-14 23:42
 */
public interface CartService {
    /**
     * 添加商品到购物车的方法
     * @param skuId 商品id
     * @param userId 用户id
     * @param skuNum 商品名称
     */
    public  void  addToCart(String skuId,String userId,Integer skuNum);

    /**
     * 查询购物车集合列表
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 缓存中没有数据,则从数据库中加载
     * @param userId
     * @return
     */
    public List<CartInfo> loadCartCache(String userId);

    /**
     * 将cookie中的数据和数据库中的数据进行合并的方法
     * @param cartListFromCookie
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId);

    void checkCart(String skuId, String isChecked, String userId);

    List<CartInfo> getCartCheckedList(String userId);

}
