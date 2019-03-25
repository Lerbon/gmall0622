package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

/**
 * @author York
 * @create 2018-11-18 17:07
 */
@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    PaymentService paymentService;

    @Override
    public String saveOrder(OrderInfo orderInfo) {
        //设置创建时间
        orderInfo.setCreateTime(new Date());
        //设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        //生成第三方支付编号
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //保存订单
        orderInfoMapper.insertSelective(orderInfo);

        //插入订单详情信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }
        //为了跳转到支付页面使用,支付会根据订单id进行支付
        String orderId = orderInfo.getId();
        return orderId;

    }

    @Override
    //生成流水号
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey, 10 * 60, tradeCode);
        jedis.close();
        return tradeCode;
    }

    @Override
    //验证流水号
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode != null) {
            return tradeCode.equals(tradeCodeNo);
        }

        return false;
    }

    @Override
    //删除流水号
    public void deleteTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        // 缺少orderDetailList
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderInfo.getId());
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetailList);

        return orderInfo;
    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderStatus(String orderId) {
        //创建工厂,创建连接,打开连接
        Connection connection = activeMQUtil.getConnection();
        //根据orderId查询出对应的orderInfo对象,然后转化为字符串
        String orderJson = initWareOrder(orderId);
        try {
            connection.start();
            //创建session
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            //创建提供者
            MessageProducer producer = session.createProducer(order_result_queue);
//            设置发送消息的内容--字符串
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            activeMQTextMessage.setText(orderJson);
            //发送消息
            producer.send(activeMQTextMessage);
            //提交事务
            session.commit();
            //关闭连接
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据orderId查询出orderInfo对象
     *
     * @param orderId
     * @return
     */
    private String initWareOrder(String orderId) {
        OrderInfo orderInfo = getOrderInfo(orderId);
        //将orderInfo对象转换为map
        Map map = initWareOrder(orderInfo);
        //将map转化为字符串
        return JSON.toJSONString(map);
    }

    @Override
    public Map initWareOrder(OrderInfo orderInfo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", "测试-->");
        map.put("deliveryAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", "2");
        map.put("wareId", orderInfo.getWareId());

        ArrayList<Object> arrayList = new ArrayList<>();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            HashMap<Object, Object> hashMap = new HashMap<>();
            hashMap.put("skuId", orderDetail.getSkuId());
            hashMap.put("skuNum", orderDetail.getSkuNum());
            hashMap.put("skuName", orderDetail.getSkuName());
            arrayList.add(hashMap);
        }
        map.put("details", arrayList);
        return map;

    }

    @Override
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {
        //创建子订单集合
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        //先查询获取原始订单
        OrderInfo orderInfoOrigin = getOrderInfo(orderId);
        //wareSkuMap反序列化
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        //遍历商品与库存对应关系数据的集合
        for (Map map : maps) {
            //获取仓库id
            String wareId = (String) map.get("wareId");
            //取出skuIds
            List<String> skuIds = (List<String>) map.get("skuIds");
            //准备生成新的子订单
            OrderInfo subOrderInfo = new OrderInfo();
            //属性拷贝,但是无法拷贝属性中的orderDetailList集合
            BeanUtils.copyProperties(orderInfoOrigin, subOrderInfo);
            //将子订单的id属性值设置为null,主键不能重复
            subOrderInfo.setId(null);
            //设置子订单中的父订单id
            subOrderInfo.setParentOrderId(orderInfoOrigin.getId());
            //设置子订单的明细,先获取原始订单明细
            List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
            //声明一个子订单的明细集合
            ArrayList<OrderDetail> orderDetailArrayList = new ArrayList<>();
            //循环数据
            for (OrderDetail orderDetail : orderDetailList) {
                //循环匹配
                for (String skuId : skuIds) {
                    //如果skuId和父订单明细相同则添加到子订单集合中
                    if (skuId.equals(orderDetail.getSkuId())) {
                        //修改子订单Id
                        orderDetail.setId(null);
                        //添加子订单明细集合
                        orderDetailArrayList.add(orderDetail);

                    }
                }
            }
            //设置总金额
            subOrderInfo.setOrderDetailList(orderDetailArrayList);
            subOrderInfo.sumTotalAmount();
            //放入仓库id
            subOrderInfo.setWareId(wareId);
            //将子订单保存至数据库
            saveOrder(subOrderInfo);
            //将新的子订单添加到子订单集合中
            subOrderInfoList.add(subOrderInfo);
        }
        //更新原始订单的状态
        updateOrderStatus(orderId, ProcessStatus.SPLIT);
        return subOrderInfoList;
    }

    @Override
    public List<OrderInfo> getExpiredOrderList() {

        //创建example对象
        Example example = new Example(OrderInfo.class);
        //创建查询体
        // select * from orderInfo where processStatus=ProcessStatus.UNPAID and expireTime<now;
        example.createCriteria().andEqualTo("processStatus", ProcessStatus.UNPAID).andLessThan("expireTime", new Date());
        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);
        // 返回所有过期订单
        return orderInfoList;

    }

    @Async
    @Override
    public void execExpiredOrder(OrderInfo orderInfo) {
        // 更新状态
        updateOrderStatus(orderInfo.getId(), ProcessStatus.CLOSED);

        // 如果用户点击了下订单，orderInfo 表中必然有数据，接着用户点击付款，则paymentInfo中必然也有数据。则把paymentInfo 中的记录也关闭掉
        paymentService.closePayment(orderInfo.getId());


    }
}
