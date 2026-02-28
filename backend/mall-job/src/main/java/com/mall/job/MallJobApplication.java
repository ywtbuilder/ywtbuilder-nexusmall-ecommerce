package com.mall.job;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan(basePackages = {"com.mall"})
@EnableScheduling
@MapperScan(basePackages = {
    "com.mall.module.product.mapper",
    "com.mall.module.cart.mapper",
    "com.mall.module.order.mapper",
    "com.mall.module.member.mapper",
    "com.mall.module.marketing.mapper",
    "com.mall.module.payment.mapper"
})
@EnableElasticsearchRepositories(basePackages = {"com.mall.module.search.repository"})
public class MallJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallJobApplication.class, args);
    }
}
