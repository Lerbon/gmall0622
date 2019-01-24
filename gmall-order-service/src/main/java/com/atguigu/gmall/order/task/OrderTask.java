package com.atguigu.gmall.order.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.service.OrderService;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @author York
 * @create 2018-11-23 11:28
 */
public class OrderTask {
    @Reference
    private OrderService orderService;

    // 每分钟的第五秒执行一次
    @Scheduled(cron = "5 * * * * ?")
    public void test01(){
        System.out.println(Thread.currentThread().getName()+"======currentThread1111");
    }

    @Scheduled(cron = "0/5 * * * * ?")
    public void test02(){
        System.out.println(Thread.currentThread().getName()+"======currentThread2222");
    }
    // 每隔20秒执行一次
    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder(){
        // 关闭过期订单 有哪些订单是过期的 {未付款， expire_time 与当前系统时间进行 }
        List<OrderInfo> orderInfoList = orderService.getExpiredOrderList();
        System.out.println("处理过期订单");
        long starttime  = System.currentTimeMillis();

        for (OrderInfo orderInfo : orderInfoList) {
            // 做订单的关闭处理
            orderService.execExpiredOrder(orderInfo);
        }
        long costtime  = System.currentTimeMillis() - starttime;

        System.out.println("一共处理"+orderInfoList.size()+"个订单 共消耗"+costtime+"毫秒");

    }
}
