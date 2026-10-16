package com.guozy.codeaigenerate.config;

import com.guozy.codeaigenerate.config.cache.MultiLevelCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 多级缓存配置（Caffeine 本地 + Redis 分布式）
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory).build();
    }

    @Bean
    @Primary
    public CacheManager multiLevelCacheManager(RedisCacheManager redisCacheManager) {
        return new MultiLevelCacheManager(redisCacheManager);
    }
}