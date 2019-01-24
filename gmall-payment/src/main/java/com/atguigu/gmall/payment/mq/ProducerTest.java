package com.atguigu.gmall.payment.mq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;

import javax.jms.*;

/**
 * @author York
 * @create 2018-11-20 18:08
 */
public class ProducerTest {
    public static void main(String[] args) throws JMSException {
        //创建连接工厂
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://192.168.168.128:61616");
        //获取连接
        Connection connection = connectionFactory.createConnection();
        //开启连接
        connection.start();
        // 创建session 第一个参数表示是否支持事务，false时，第二个参数Session.AUTO_ACKNOWLEDGE，Session.CLIENT_ACKNOWLEDGE，DUPS_OK_ACKNOWLEDGE其中一个
        // 第一个参数设置为true时，第二个参数可以忽略 服务器设置为SESSION_TRANSACTED
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        //创建队列
        Queue queue = session.createQueue("Atguigu");
        //消息提供者
        MessageProducer producer = session.createProducer(queue);
        //创建消息对象
        ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
        activeMQTextMessage.setText("Hello,World!!!");
        //发送消息
        producer.send(activeMQTextMessage);
        producer.close();
        session.close();
        connection.close();


    }
}
