package com.project.CrisisGrid.crisis_service.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CRISIS_CACHE = "crisisCache";

    public static final String GEO_QUERY_CACHE = "geoQueryCache";

    public static final String CRISIS_STATS_CACHE = "crisisStatsCache";

    /**
     * Default Cache Configuration
     */
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(redisSerializer())
                );
    }

    /**
     * Custom TTL per cache
     *
     *
     */

    @Bean
    public GenericJackson2JsonRedisSerializer redisSerializer() {

        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());

        mapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );

        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {

        return builder -> builder

                .withCacheConfiguration(
                        CRISIS_CACHE,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(15))
                                .disableCachingNullValues()
                )

                .withCacheConfiguration(
                        GEO_QUERY_CACHE,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(5))
                                .disableCachingNullValues()
                )

                .withCacheConfiguration(
                        CRISIS_STATS_CACHE,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30))
                                .disableCachingNullValues()
                );
    }

    /**
     * Redis Template
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template =
                new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);

        // Key serializer
        template.setKeySerializer(
                new StringRedisSerializer()
        );

        template.setHashKeySerializer(
                new StringRedisSerializer()
        );

        // Value serializer
        template.setValueSerializer(
                redisSerializer()
        );

        template.setHashValueSerializer(
                redisSerializer()
        );

        template.setEnableTransactionSupport(true);

        template.afterPropertiesSet();

        return template;
    }
}