package com.mall.job.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列/交换机/绑定配置
 */
@Configuration
public class RabbitMqConfig {

    // ========== 订单超时取消 ==========
    public static final String ORDER_TTL_EXCHANGE = "mall.order.direct";
    public static final String ORDER_TTL_QUEUE = "mall.order.ttl";
    public static final String ORDER_CANCEL_QUEUE = "mall.order.cancel";
    public static final String ORDER_TTL_ROUTING_KEY = "order.ttl";
    public static final String ORDER_CANCEL_ROUTING_KEY = "order.cancel";

    @Bean
    public DirectExchange orderDirectExchange() {
        return new DirectExchange(ORDER_TTL_EXCHANGE);
    }

    /** 延迟队列 —— 消息 TTL 到期后进入死信队列 */
    @Bean
    public Queue orderTtlQueue() {
        return QueueBuilder.durable(ORDER_TTL_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_TTL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_CANCEL_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderCancelQueue() {
        return QueueBuilder.durable(ORDER_CANCEL_QUEUE).build();
    }

    @Bean
    public Binding orderTtlBinding() {
        return BindingBuilder.bind(orderTtlQueue()).to(orderDirectExchange()).with(ORDER_TTL_ROUTING_KEY);
    }

    @Bean
    public Binding orderCancelBinding() {
        return BindingBuilder.bind(orderCancelQueue()).to(orderDirectExchange()).with(ORDER_CANCEL_ROUTING_KEY);
    }

    // ========== ES 商品同步 ==========
    public static final String PRODUCT_SYNC_EXCHANGE = "mall.product.topic";
    public static final String PRODUCT_SYNC_QUEUE = "mall.product.es.sync";

    @Bean
    public TopicExchange productTopicExchange() {
        return new TopicExchange(PRODUCT_SYNC_EXCHANGE);
    }

    @Bean
    public Queue productSyncQueue() {
        return QueueBuilder.durable(PRODUCT_SYNC_QUEUE).build();
    }

    @Bean
    public Binding productSyncBinding() {
        return BindingBuilder.bind(productSyncQueue()).to(productTopicExchange()).with("product.#");
    }
}
