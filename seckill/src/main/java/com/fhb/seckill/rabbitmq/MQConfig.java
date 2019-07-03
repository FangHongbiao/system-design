package com.fhb.seckill.rabbitmq;

import org.springframework.amqp.core.*;;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.Bidi;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by IntelliJ IDEA 2016.3.3.
 * User: fhb
 * Email: fhb7218@gmail.com
 * Date: 2019/7/2
 * Time: 16:33
 *
 * @author hbfang
 */

@Configuration
public class MQConfig {

    public static final String QUEUE = "queue";
    public static final String HEADER_QUEUE = "header.queue";
    public static final String TOPIC_QUEUE1 = "topic.queue1";
    public static final String TOPIC_QUEUE2 = "topic.queue2";
    public static final String TOPIC_EXCHANGE = "topicExchange";
    public static final String FANOUT_EXCHANGE = "fanoutExchange";
    public static final String  HEADERS_EXCHANGE = "headersExchange";
    public static final String MIAOSHA_QUEUE = "miaosha.queue";



    @Bean
    public Queue miaoshaQueue() {
        return new Queue(MIAOSHA_QUEUE, true);
    }

    /**
     * Direct 模式 Exchange
     * @return
     */
    @Bean
    public Queue queue() {
        return new Queue(QUEUE, true);
    }

    /**
     * Topic 模式 Exchange
     * @return
     */
    @Bean
    public Queue topicQueue1() {
        return new Queue(TOPIC_QUEUE1, true);
    }

    @Bean
    public Queue topicQueue2() {
        return new Queue(TOPIC_QUEUE2, true);
    }

    @Bean
    public TopicExchange topicExchange () {
        return new TopicExchange(TOPIC_EXCHANGE);
    }

    @Bean
    public Binding topicBinding1() {
        return BindingBuilder.bind(topicQueue1()).to(topicExchange()).with("topic.key1");
    }

    @Bean
    public Binding topicBinding2() {
        return BindingBuilder.bind(topicQueue2()).to(topicExchange()).with("pick");
    }


    /**
     * Fanout 广播 Exchange
     * @return
     */
    @Bean
    public FanoutExchange fanoutExchange () {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Binding fanoutBinding1() {
        return  BindingBuilder.bind(topicQueue1()).to(fanoutExchange());
    }

    @Bean
    public Binding fanoutBinding2() {
        return  BindingBuilder.bind(topicQueue2()).to(fanoutExchange());
    }


    /**
     * Headers 模式 Exchange
     * @return
     */
    @Bean
    public HeadersExchange headersExchange () {
        return new HeadersExchange(HEADERS_EXCHANGE);
    }

    @Bean
    public Queue headerQueue1() {
        return new Queue(HEADER_QUEUE, true);
    }

    @Bean
    public Binding headerBinding() {

        Map<String, Object> map = new HashMap<>();

        map.put("header1", "value1");
        map.put("header2", "value2");

        return  BindingBuilder.bind(headerQueue1()).to(headersExchange()).whereAll(map).match();
    }

}
