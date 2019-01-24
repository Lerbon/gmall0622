package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * //未登录的时候需要往cookie中存储数据
 *
 * @author York
 * @create 2018-11-15 9:58
 */
@Component
public class CartCookieHandler {

    //定义购物车名称
    private String COOKIECARTNAME = "CART";
    //设置cookie过期时间
    private int COOKIE_CART_MAXAGE = 7 * 24 * 3600;

    @Reference
    private ManageService manageService;


    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, int skuNum) {
        //判断cookie中是否有购物车,添加的购物新信息包含中文 ,所以要进行编码
        String cartJson = CookieUtil.getCookieValue(request, COOKIECARTNAME, true);
        List<CartInfo> cartInfoArrayList = new ArrayList<>();
        boolean ifExist = false;
        //cookie中存在对应的购物车
        if (cartJson != null) {
            //将购物车数据转化为对象的集合
            cartInfoArrayList = JSON.parseArray(cartJson, CartInfo.class);
            //对获取的购物车集合进行遍历
            for (CartInfo cartInfo : cartInfoArrayList) {
                //购物车中有对应的商品
                if (cartInfo.getSkuId().equals(skuId)) {
                    //更新数量
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                    //价格设置
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    ifExist = true;
                    break;
                }
            }
        }
        //购物车里没有对应的商品或者没有购物车
        if (!ifExist) {
            //获取商品信息新增到购物车
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            cartInfoArrayList.add(cartInfo);
        }
        //将购物车存储到cookie中
        String newCartJson = JSON.toJSONString(cartInfoArrayList);
        CookieUtil.setCookie(request, response, COOKIECARTNAME, newCartJson, COOKIE_CART_MAXAGE, true);
    }

    /**
     * 查询cookie中的购物车信息
     *
     * @param request
     * @return
     */
    public List<CartInfo> getCartList(HttpServletRequest request) {

        String cartJson = CookieUtil.getCookieValue(request, COOKIECARTNAME, true);
        List<CartInfo> cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
        return cartInfoList;
    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {

        CookieUtil.deleteCookie(request, response, COOKIECARTNAME);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
        //取出购物车中的商品
        List<CartInfo> cartList = getCartList(request);
        //循环比较
        for (CartInfo cartInfo : cartList) {
            //找到cookie中对应的商品更改其isChecked属性值
            if (cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }
        }
        //再重新保存到cookie中
        String newCartJson = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request,response,COOKIECARTNAME,newCartJson,COOKIE_CART_MAXAGE,true);

    }
}
