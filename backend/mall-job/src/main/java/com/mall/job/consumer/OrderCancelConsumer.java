package com.mall.job.consumer;

import com.mall.job.config.RabbitMqConfig;
import com.mall.module.order.service.PortalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderCancelConsumer {

    private final PortalOrderService portalOrderService;

    public OrderCancelConsumer(PortalOrderService portalOrderService) {
        this.portalOrderService = portalOrderService;
    }

    @RabbitListener(queues = RabbitMqConfig.ORDER_CANCEL_QUEUE)
    public void handle(Long orderId) {
        log.info("收到订单超时取消消息，orderId={}", orderId);
        try {
            portalOrderService.cancelOrder(orderId);
            log.info("订单超时取消成功，orderId={}", orderId);
        } catch (Exception e) {
            log.error("订单超时取消失败，orderId={}", orderId, e);
        }
    }
}
