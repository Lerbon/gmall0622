package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

/**
 * @author York
 * @create 2018-11-19 18:48
 */
public interface PaymentService {

    /**
     * 保存数据
     * @param paymentInfo
     */
    void savePaymentInfo(PaymentInfo paymentInfo);

    /**
     * 根据out_trade_no 查询PaymentInfo对象
     * @param paymentInfoQuery
     * @return
     */
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    /**
     * 更新的方法
     * @param out_trade_no
     * @param paymentInfoUpd
     */
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    /**
     * 发送通知
     * @param paymentInfo
     * @param result
     */
    public void sendPaymentResult(PaymentInfo paymentInfo,String result);


    /**
     * 检查订单是否已支付
     * @param paymentInfoQuery
     * @return
     */
    boolean checkPayment(PaymentInfo paymentInfoQuery);


    /**
     * 发送延时队列的方法
     * @param outTradeNo 单号
     * @param delaySec  延迟时长,秒
     * @param checkCount 检查次数
     */
    public void sendDelayPaymentResult(String outTradeNo,int delaySec,int checkCount);

    /**
     * 关闭过期订单
     * @param id
     */
    void closePayment(String id);
}
