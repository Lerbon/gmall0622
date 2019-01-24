package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserInfoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author York
 * @create 2018-10-31 19:05
 */
@Controller
public class OrderController {

    //调用购物车服务
    @Reference
    private CartService cartService;

    //调用用户服务
    @Reference
    private UserInfoService userInfoService;

    @Reference
    private OrderService orderService;

    @RequestMapping("trade")
    @LoginRequire(autoRedirect = true)
    public String trade(HttpServletRequest request) {
        //收货人信息展示,根据用户id查询送货地址列表
        //在单点登录的时候已经将userId放入request域中
        String userId = (String) request.getAttribute("userId");
        //得到选中的购物车列表
        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);
        //获取收货人地址列表
        List<UserAddress> userAddressList = userInfoService.getUserAddressList(userId);
        request.setAttribute("userAddressList", userAddressList);
        // 循环购物车被选中的商品，赋给orderDetail
        // 声明一个OrderDetail 集合
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            //将赋值完的orderDetial对象放入集合中
            orderDetailList.add(orderDetail);
        }
        //总价格
        OrderInfo orderInfo = new OrderInfo();
        //// orderInfo 如何计算总价格 -- 把orderDetailList集合赋给orderInfo，然后orderInfo 中有方法自动计算总价格
        orderInfo.setOrderDetailList(orderDetailList);
        //该方法将计算结果赋值给totalAmount
        orderInfo.sumTotalAmount();

        // 取得流水号，并保存到redis中
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        //将orderDetail放入作用域
        request.setAttribute("orderDetailList", orderDetailList);
        //将总价格放入作用域
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        //将地址列表放入作用域
        request.setAttribute("userAddressList",userAddressList);
        return "trade";
    }


    @RequestMapping("submitOrder")
    @LoginRequire
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){
        String userId = (String)request.getAttribute("userId");
        //获取tradeNo
        String tradeNo = request.getParameter("tradeNo");
        //跟redis中的数据作对比,如果相同的话则可以跳转页面,跳转的同时将redis中的tradeNo删除,后续重复提交的话则无法从redis中找出匹配的数据
        boolean result = orderService.checkTradeCode(userId, tradeNo);
        if (!result){
            request.setAttribute("errMsg","请刷新后提交订单");
            return "tradeFail";
        }
        //初始化参数
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        orderInfo.sumTotalAmount();
        orderInfo.setUserId(userId);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        //获取订单详情表中的每件商品
        for (OrderDetail orderDetail : orderDetailList) {
            //验证库存
            boolean flag = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if(!flag){
                request.setAttribute("errMsg","库存不足，修改购买数量！");
                return "tradeFail";
            }
        }

        //保存
        String orderId = orderService.saveOrder(orderInfo);

        // 将redis 中的tradeNo 删除
        orderService.deleteTradeNo(userId);
        //重定向
        return "redirect://payment.gmall.com/index?orderId="+orderId;

    }

    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");
        List<OrderInfo> subOrderInfoList = orderService.splitOrder(orderId,wareSkuMap);
        ArrayList<Map> wareMapList = new ArrayList<>();
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            wareMapList.add(map);
        }
        return JSON.toJSONString(wareMapList);

    }
}
