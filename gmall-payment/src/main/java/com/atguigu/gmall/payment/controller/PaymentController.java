package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author York
 * @create 2018-11-19 18:12
 */
@Controller
public class PaymentController {
    @Reference
    OrderService orderService;

    @Reference
    PaymentService paymentService;

    @Autowired
    AlipayClient alipayClient;

    @RequestMapping("index")
    public String index(String orderId, HttpServletRequest request) {
//        //获取订单id
//        String orderId = request.getParameter("orderId");
//        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
//        map.put("orderId",orderId);
//        map.put("totalAmount",orderInfo.getTotalAmount());
//        return "index";
        // 得到orderId
        System.out.println("orderId" + orderId);
        request.setAttribute("orderId", orderId);
        // 得到总金额,应该在OrderInfo 中 根据orderId 查询OrderInfo对应的总金额
        // select * from orderInfo where orderId = ?
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        // 保存总金额
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        // nickName,只要登录则会自动获取！
        return "paymentIndex";
    }

    @RequestMapping(value = "/alipay/submit", method = RequestMethod.POST)
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response) {
        //获取订单的id
        String orderId = request.getParameter("orderId");
        //取得订单信息
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        //保存支付信息
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject("购买手机");
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setCreateTime(new Date());
        //保存信息
        paymentService.savePaymentInfo(paymentInfo);

        //支付宝参数,主要作用是生成二维码,涉及到参数和签名
        //注入到spring容器中
        AlipayTradePagePayRequest alipayTradePagePayRequest = new AlipayTradePagePayRequest();//创建API对应的request
        //同步回调地址
        alipayTradePagePayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        //异步回调地址,在公共参数中设置回调和通知地址
        alipayTradePagePayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        //声明一个map集合
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", paymentInfo.getOutTradeNo());
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", paymentInfo.getTotalAmount());
        map.put("subject", paymentInfo.getSubject());
        //将map转化为字符串
        String mapJson = JSON.toJSONString(map);
        alipayTradePagePayRequest.setBizContent(mapJson);
        String form = "";
        try {
            form = alipayClient.pageExecute(alipayTradePagePayRequest).getBody();//调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        response.setContentType("text/html;charset=UTF-8");

        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(),15,3);
        return form;
    }

    @RequestMapping(value = "/alipay/callback/return", method = RequestMethod.GET)
    public String callbackReturn() {
        return "redirect:" + AlipayConfig.return_order_url;
    }

    @RequestMapping(value = "/alipay/callback/notify", method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String, String> paramMap, HttpServletRequest request) throws AlipayApiException {
//        Map<String,String>paramMap = ...将异步通知中收到的所有参数都存放在map中
//        boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap, ALIPAY_PUBLIC_KEY, CHARSET, SIGN_TYPE) //调用SDK验证签名
//        if(signVerfied){
//            TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验trade_status[TRADE_SUCCESS,TRADE_FINISHED]，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
//        }else{
//            TODO 验签失败则记录异常日志，并在response中返回failure.
//        }
        //调用SDK验证签名
        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, AlipayConfig.charset, AlipayConfig.sign_type);
        if (!flag) {
            return "fail";
        }
        // 获取交易状态 TRADE_SUCCESS,TRADE_FINISHED ,
        String trade_status = paramMap.get("trade_status");
        // payment_status为支付状态PAID支付！通过out_trade_no 第三方交易编号。
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
            // 判断当前用户是PAID ,UNPAID.
            String out_trade_no = paramMap.get("out_trade_no");
            // select * from paymentInfo where out_trade_no = out_trade_no;
            PaymentInfo paymentInfoQuery = new PaymentInfo();
            paymentInfoQuery.setOutTradeNo(out_trade_no);

            PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
            // 当支付宝发现该笔订单已经关闭，或者是已经付款了，则告诉商家付款失败！
            if (paymentInfo.getPaymentStatus() == PaymentStatus.ClOSED || paymentInfo.getPaymentStatus() == PaymentStatus.PAID) {
                return "fail";
            } else {
                // 未付款的时候PaymentStatus.UNPAID 当交易成功 将PaymentStatus.UNPAID 改为PaymentStatus.PAID
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                // 回调时间
                paymentInfoUpd.setCallbackTime(new Date());
                // 异步回调的内容
                paymentInfoUpd.setCallbackContent(paramMap.toString());
                sendPaymentResult(paymentInfoUpd,"success");
                // update paymentInfo set payment_status=PAID where out_trade_no = out_trade_no;
                paymentService.updatePaymentInfo(out_trade_no, paymentInfoUpd);
                return "success";
            }
        }
        return "fail";
    }

    //发送验证
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo, @RequestParam("result") String result) {
        paymentService.sendPaymentResult(paymentInfo, result);
        return "sent payment result";
    }

    /**
     *  查看那个一个订单是否付款成功！
     * @return
     */
    @RequestMapping("queryPaymentResult")
    @ResponseBody
    public String queryPaymentResult(String orderId){
        // 根据orderId 查询出去交易对象
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
        // 调用服务层的方法，得到交易对象中的out_trade_no
        boolean flag = paymentService.checkPayment(paymentInfoQuery);
        return ""+flag;

    }

}
