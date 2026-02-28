package com.mall.test;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * 集成测试基类 — TestContainers 启动 MySQL + Redis
 * <p>
 * 子类继承后使用 {@code @SpringBootTest} 即可获得真实数据库环境。
 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("mall_test")
            .withUsername("test")
            .withPassword("test")
            .withCommand("--character-set-server=utf8mb4", "--collation-server=utf8mb4_unicode_ci");

    @SuppressWarnings("resource")
    @Container
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        // Redis
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
        // Flyway
        registry.add("spring.flyway.enabled", () -> "false");
        // MongoDB — 禁用（集成测试可能不需要 Mongo）
        registry.add("spring.data.mongodb.uri", () -> "mongodb://localhost:27017/test");
        registry.add("spring.autoconfigure.exclude", () ->
                "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration");
        // RabbitMQ — 禁用
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5672");
        registry.add("spring.autoconfigure.exclude",
                () -> "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration," +
                      "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
                      "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration");
        // JWT test secret
        registry.add("mall.auth.jwt-secret", () -> "test-secret-key-for-integration-testing-32chars-at-least!");
        registry.add("mall.auth.jwt-expiration-seconds", () -> "3600");
    }
}
