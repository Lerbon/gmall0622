package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.CookieHandler;
import java.util.List;

/**
 * @author York
 * @create 2018-11-14 23:36
 */
@Controller
public class CartController {

    @Reference
    CartService cartService;

    @Autowired
    CartCookieHandler cartCookieHandler;

    @Reference
    ManageService manageService;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)//标明不需要登录即可添加商品到购物车
    public String addToCart(HttpServletRequest request, HttpServletResponse response) {
        String userId = (String) request.getAttribute("userId");
//        request.getParameter可以用于获取前台提交的表单数据
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");

        //userId不为空表示已登录
        if (userId != null) {
            cartService.addToCart(skuId, userId, Integer.parseInt(skuNum));
        } else {
            //说明用户没有登录,没有登录的话购物车需要存放到cookie中
            cartCookieHandler.addToCart(request, response, skuId, userId, Integer.parseInt(skuNum));
        }
        //获取skuInfo对象返回至前台页面
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);
        return "success";
    }

    /**
     * 显示购物车列表
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request, HttpServletResponse response) {
        //判断用户是否已经登录,登录了就从redis中获取数据,redis中没有就从数据库中获取
        //没有登录的话就从cookie中获取
        String userId = (String) request.getAttribute("userId");
        //如果用户已经登录
        if (userId != null) {
            //先从cookie中查找购物车
            List<CartInfo> cartListFromCookie = cartCookieHandler.getCartList(request);
            List<CartInfo> cartInfoList = null;
            //如果cookie中有数据
            if (cartListFromCookie != null && cartListFromCookie.size() > 0) {
                //开始合并
                cartInfoList = cartService.mergeToCartList(cartListFromCookie, userId);
                //合并完成删除cookie中的购物车
                cartCookieHandler.deleteCartCookie(request, response);
            } else {
                //如果cookie中没有则从数据库中查询获得
                //从redis中获取数据,或者从数据库中获取
                cartInfoList = cartService.getCartList(userId);
            }


            request.setAttribute("cartList", cartInfoList);
        } else {
            //用户未登录的话从cookie中获取
            List<CartInfo> cartInfoList = cartCookieHandler.getCartList(request);
            request.setAttribute("cartList", cartInfoList);
        }
        return "cartList";
    }

    @RequestMapping("checkCart")
    @ResponseBody
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request, HttpServletResponse response) {
        //var param="isChecked="+isCheckedFlag+"&"+"skuId="+skuId;这是前台发起的请求
        //获取url中参数的值
        String skuId = request.getParameter("skuId");
        String isChecked = request.getParameter("isChecked");
        String userId = (String) request.getAttribute("userId");
        if (userId != null) {
            cartService.checkCart(skuId, isChecked, userId);
        } else {
            cartCookieHandler.checkCart(request, response, skuId, isChecked);
        }


    }

    /**
     * 用户在订单页点击去结算按键,要求没有登录的用户必须先登录
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("toTrade")
    @LoginRequire(autoRedirect = true)
    public String toTrade(HttpServletRequest request, HttpServletResponse response) {
        String userId = (String) request.getAttribute("userId");
        //先将cookie中的购物车数据和数据库中的数据进行合并
        List<CartInfo> cartInfoListCK = cartCookieHandler.getCartList(request);
        if (cartInfoListCK != null && cartInfoListCK.size() > 0) {
            //如果cookie中有数据则进行合并
            cartService.mergeToCartList(cartInfoListCK, userId);
            //合并完成之后将cookie中的数据删除
            cartCookieHandler.deleteCartCookie(request, response);

        }
        //重定向到订单
        return "redirect://order.gmall.com/trade";

    }

}
