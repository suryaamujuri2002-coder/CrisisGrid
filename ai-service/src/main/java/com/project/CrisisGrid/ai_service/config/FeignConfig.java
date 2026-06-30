package com.project.CrisisGrid.ai_service.config;

import feign.Logger;
import feign.okhttp.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * FIX — PATCH support for Feign.
     *
     * Java's built-in HttpURLConnection (Feign's default HTTP client)
     * does NOT support the PATCH method and throws:
     *   "Invalid HTTP method: PATCH"
     *
     * Registering a feign.okhttp.OkHttpClient bean here forces Feign
     * to use OkHttp for every request, which supports PATCH natively.
     *
     * This approach is more reliable than feign.okhttp.enabled=true
     * in application.yml because it works regardless of Spring Cloud
     * auto-configuration order.
     *
     * Requires the feign-okhttp dependency in pom.xml:
     *   <dependency>
     *     <groupId>io.github.openfeign</groupId>
     *     <artifactId>feign-okhttp</artifactId>
     *   </dependency>
     */
    @Bean
    public OkHttpClient feignOkHttpClient() {
        return new OkHttpClient();
    }

    /**
     * NOTE: The JWT RequestInterceptor has been intentionally removed.
     *
     * The Feign calls made by ai-service (to crisis-service and
     * resource-service) are internal service-to-service calls —
     * they are NOT made on behalf of a logged-in user.
     *
     * The Kafka consumer thread has NO SecurityContext, so
     * SecurityContextHolder.getContext().getAuthentication() always
     * returns null here, making the interceptor a no-op anyway.
     *
     * More importantly, crisis-service's /api/v1/crisis/{id}/ai-analysis
     * is now permitAll() (as fixed in SecurityConfig), so no JWT is
     * needed for that call. resource-service also permitAll() all requests.
     *
     * If you add endpoints later that DO need auth between services,
     * use a service account token or Spring Security's inter-service
     * auth mechanism instead of the SecurityContext approach.
     */
}