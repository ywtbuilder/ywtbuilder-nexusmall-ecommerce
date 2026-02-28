package com.mall.job.consumer;

import com.mall.job.config.RabbitMqConfig;
import com.mall.module.search.service.EsProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EsProductSyncConsumer {

    private final EsProductService esProductService;

    public EsProductSyncConsumer(EsProductService esProductService) {
        this.esProductService = esProductService;
    }

    @RabbitListener(queues = RabbitMqConfig.PRODUCT_SYNC_QUEUE)
    public void handle(Long productId) {
        log.info("收到商品ES同步消息，productId={}", productId);
        try {
            esProductService.create(productId);
            log.info("商品ES同步成功，productId={}", productId);
        } catch (Exception e) {
            log.error("商品ES同步失败，productId={}", productId, e);
        }
    }
}
