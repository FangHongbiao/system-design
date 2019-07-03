package com.fhb.seckill.rabbitmq;

import com.fhb.seckill.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/7/2
 * Time: 16:32
 *
 * @author hbfang
 */

@Service
public class MQSender {

    private static Logger log = LoggerFactory.getLogger(MQSender.class);


    @Autowired
    AmqpTemplate amqpTemplate;



    // --------------------------------start test rabbitmq----------------------------------------
    public void send(Object message) {

        String msg = RedisService.beanToString(message);
        log.info("send message: " + msg);
        amqpTemplate.convertAndSend(MQConfig.QUEUE, msg);
    }

    public void sendTopic(Object message) {

        String msg = RedisService.beanToString(message);
        log.info("send message: " + msg);
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key1", msg + "1");
        amqpTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE, "topic.key2", msg + "2");
    }

    public void sendFanout(Object message) {

        String msg = RedisService.beanToString(message);
        log.info("send message: " + msg);
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE, "", msg + "1");
    }

    public void sendHeader(Object message) {

        String msg = RedisService.beanToString(message);
        log.info("send message: " + msg);

        MessageProperties properties = new MessageProperties();
        properties.setHeader("header1", "value1");
        properties.setHeader("header2", "value2");
        Message obj = new Message(msg.getBytes(), properties);
        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE, "", obj);
    }
    // --------------------------------end test rabbitmq----------------------------------------


    public void sendMiaoshaMessage(MiaoshaMessage mm) {

        String msg = RedisService.beanToString(mm);

        log.info("send message: " + msg);

        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE, msg);

    }
}
