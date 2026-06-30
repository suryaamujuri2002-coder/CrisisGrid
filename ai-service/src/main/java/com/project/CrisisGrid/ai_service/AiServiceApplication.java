package com.project.CrisisGrid.ai_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * FIX — exclude all DataSource / JPA auto-configuration.
 *
 * ai-service has no database of its own. It only uses:
 *   - Kafka  (consume crisis.created events)
 *   - OpenAI via Spring AI (run classifier / scorer agents)
 *   - Feign  (call crisis-service and resource-service)
 *   - Redis  (optional caching)
 *
 * The compile-time classpath includes JPA jars because resource-service
 * classes are on the path, which causes Spring Boot to attempt DataSource
 * auto-configuration and fail with:
 *   "Failed to determine a suitable driver class"
 *
 * The exclude list here mirrors the application.yml exclusions — both files
 * are now in sync so the service starts cleanly regardless of which
 * mechanism Spring Boot picks up first.
 */
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        SqlInitializationAutoConfiguration.class,
})
@EnableKafka
@EnableFeignClients
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }
}