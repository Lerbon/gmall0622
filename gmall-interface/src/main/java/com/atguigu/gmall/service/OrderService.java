package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

/**
 * @author York
 * @create 2018-11-18 17:08
 */
public interface OrderService {
    // 做保存
    String  saveOrder(OrderInfo orderInfo);
    // 生成流水号
    String getTradeNo(String userId);
    // 验证tradeNo
    boolean checkTradeCode(String userId,String tradeCodeNo);
    // 删除tradeNo
    void deleteTradeNo(String userId);
    // 验证库存
    boolean checkStock(String skuId, Integer skuNum);

    //根据orderId获取orderInfo
    OrderInfo getOrderInfo(String orderId);

    //根据orderI更新订单进程状态
    void updateOrderStatus(String orderId, ProcessStatus paid);

    //通知库存系统订单状态
    void sendOrderStatus(String orderId);

    //将orderInfo对象转换为map集合
    public Map initWareOrder(OrderInfo orderInfo);

    //根据订单id和商品仓库对照关系进行拆弹
    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);

    /**
     * 获取过期订单
     * @return
     */
    List<OrderInfo> getExpiredOrderList();

    /**
     * 处理过期订单
     * @param orderInfo
     */
    void execExpiredOrder(OrderInfo orderInfo);
}
