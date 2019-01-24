package com.atguigu.gmall.order.mq;

import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.service.OrderService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;

import javax.jms.JMSException;

/**
 * @author York
 * @create 2018-11-20 20:14
 */
public class OrderConsumer {
    @Autowired
    OrderService orderService;

    // 怎么消费，利用消息监听器工厂来监听消息，一旦得知有消息，则立即消费
    // 发送消息的内容 {result=success, orderId=112}
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE",containerFactory ="jmsQueueListener" )
    public void consumerPaymentResult(ActiveMQMapMessage activeMQMapMessage) throws JMSException {
        String result = activeMQMapMessage.getString("result");
        String orderId = activeMQMapMessage.getString("orderId");

        //判断取得的result状态
        if ("success".equals(result)){
            //更改订单状态
            orderService.updateOrderStatus(orderId,ProcessStatus.PAID);
            /*根据订单orderId进行减库存
            *   发送消息告诉库存系统
            *   更新订单状态,发货,或者等待发货
            * */
            orderService.sendOrderStatus(orderId);
        }


    }

}
