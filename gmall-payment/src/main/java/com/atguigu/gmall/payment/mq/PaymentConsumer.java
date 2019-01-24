package com.atguigu.gmall.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.service.PaymentService;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;

/**
 * @author York
 * @create 2018-11-23 11:07
 */
@Component
public class PaymentConsumer {
    @Reference
    private PaymentService paymentService;

    //利用消息监听器工厂来监听消息,一旦得知有消息则立即消费
    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(ActiveMQMapMessage activeMQMapMessage) throws JMSException {
        String outTradeNo = activeMQMapMessage.getString("outTradeNo");
        int delaySec = activeMQMapMessage.getInt("delaySec");
        int checkCount = activeMQMapMessage.getInt("checkCount");
        //检查支付宝的支付结果,主要根据条件是outTradeNo
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        System.out.println("开始查询");
        boolean flag = paymentService.checkPayment(paymentInfo);
        //如果查询结果为false证明用户未付款,检查次数还有的话就继续查询
        if (!flag&&checkCount>0){
            //调用check方法继续查询
            paymentService.sendDelayPaymentResult(outTradeNo,delaySec,checkCount-1);

        }
    }
}
