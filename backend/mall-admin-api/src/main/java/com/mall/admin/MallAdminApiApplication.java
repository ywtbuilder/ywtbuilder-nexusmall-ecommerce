package com.mall.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.mall"})
@ConfigurationPropertiesScan(basePackages = {"com.mall"})
@MapperScan(basePackages = {
    "com.mall.admin.mapper",
    "com.mall.module.product.mapper",
    "com.mall.module.cart.mapper",
    "com.mall.module.order.mapper",
    "com.mall.module.member.mapper",
    "com.mall.module.marketing.mapper",
    "com.mall.module.payment.mapper"
})
@EnableElasticsearchRepositories(basePackages = {"com.mall.module.search.repository"})
@EnableMongoRepositories(basePackages = {"com.mall.module.member.repository"})
public class MallAdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallAdminApiApplication.class, args);
    }
}
